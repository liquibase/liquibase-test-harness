package liquibase.harness.generateChangelog

import liquibase.Scope
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.CommandExecutionException
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.rollback.RollbackStrategy
import liquibase.ui.UIService
import org.junit.Assume
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static GenerateChangelogTestHelper.*
import static liquibase.harness.util.TestUtils.*
import static liquibase.harness.util.FileUtils.*

class GenerateChangelogTest extends Specification {
    @Shared
    RollbackStrategy strategy
    @Shared
    List<DatabaseUnderTest> databases
    @Shared
    UIService uiService = Scope.getCurrentScope().getUI()
    String testResourcesPath = System.getProperty("user.dir") + "/src/test/resources/"
    String generatedResourcesPath = "/src/test/resources/"
    long timeMillisBeforeTest
    long timeMillisAfterTest

    def setupSpec() {
        databases = TestConfig.instance.getFilteredDatabasesUnderTest()
        strategy = chooseRollbackStrategy()
        strategy.prepareForRollback(databases)
    }

    @Unroll
    def "apply generateChangelog test for #testInput.change against #testInput.databaseName #testInput.version"() {
        given: "read input data for generateChangelog test"
        Map<String, Object> argsMap = new HashMap()
        argsMap.put("url", testInput.url)
        argsMap.put("username", testInput.username)
        argsMap.put("password", testInput.password)
        boolean shouldRunChangeSet

        and: "check database under test is online"
        def connection = testInput.database.getConnection()
        shouldRunChangeSet = connection instanceof JdbcConnection
        assert shouldRunChangeSet: "Database ${testInput.databaseName} ${testInput.version} is offline!"

        and: "ignore testcase if it's invalid for this combination of db type and/or version"
        shouldRunChangeSet = !getResourceContent("/$testInput.sqlChangelogPath").toLowerCase()?.contains("invalid test")
        Assume.assumeTrue("INFO: Test for $testInput.change is ignored", shouldRunChangeSet)

        and: "testing generateChangelog command for all files format"
        def map = new LinkedHashMap<String, String>()
        map.put("expectedXmlChangelog", testInput.xmlChangelogPath)
        map.put("expectedSqlChangelog", testInput.sqlChangelogPath)
        map.put("expectedYmlChangelog", testInput.xmlChangelogPath.replace(".xml", ".yml"))
        map.put("expectedJsonChangelog", testInput.xmlChangelogPath.replace(".xml", ".json"))
        argsMap.put("excludeObjects", "(?i)posts, (?i)authors")//excluding static test-harness objects from generated changelog
        String sqlSpecificChangelogFile

        for (Map.Entry<String, String> entry : map.entrySet()) {

            when: "execute generateChangelog command using different changelog formats"
            argsMap.put("changeLogFile", testInput.xmlChangelogPath)
            executeCommandScope("update", argsMap)
            argsMap.put("changeLogFile", testResourcesPath + entry.value)
            if (entry.key.equalsIgnoreCase("expectedSqlChangelog")) {
                def shortDbName = getShortDatabaseName(testInput.databaseName)
                sqlSpecificChangelogFile = entry.value.replace(".sql", ".$shortDbName" + ".sql")
                argsMap.put("changeLogFile", testResourcesPath + sqlSpecificChangelogFile)
            }
            executeCommandScope("generateChangelog", argsMap, testInput.databaseName)

            then: "check if a changelog was actually generated and validate it's content"
            String generatedChangelog = readFile((String) argsMap.get("changeLogFile"))
            if (entry.key.equalsIgnoreCase("expectedSqlChangelog")) {
                validateSqlChangelog(getResourceContent("/$entry.value"), generatedChangelog)
            } else {
                assert generatedChangelog.contains("$testInput.change")
            }

            when: "get sql generated for the change set"
            String generatedSql
            argsMap.put("changeLogFile", generatedResourcesPath + entry.value)
            if (!entry.key.equalsIgnoreCase("expectedSqlChangelog")) {
//                argsMap.put("changeLogFile", generatedResourcesPath + getSqlSpecificChangelogFile(testInput.databaseName, entry.value))
                generatedSql = parseQuery(executeCommandScope("updateSql", argsMap).toString())
            }


            then: "execute updateSql command on generated changelogs"
            if (!entry.key.equalsIgnoreCase("expectedSqlChangelog")) {
                def expectedSql = parseQuery(getSqlFileContent(testInput.change, testInput.databaseName, testInput.version,
                        "liquibase/harness/generateChangelog/expectedSql"))
                def generatedSqlIsCorrect = generatedSql == expectedSql
                if (!generatedSqlIsCorrect) {
                    Scope.getCurrentScope().getUI().sendMessage("FAIL! Expected sql doesn't " +
                            "match generated sql! Deleting expectedSql file will test that new sql works correctly and " +
                            "will auto-generate a new version if it passes. \nEXPECTED SQL: \n" + expectedSql + " \n" +
                            "GENERATED SQL: \n" + generatedSql)
                    assert generatedSql == expectedSql
                }
            }


            and: "rollback changes"
            argsMap.put("changeLogFile", testInput.xmlChangelogPath)
            strategy.performRollback(argsMap)
        }

        cleanup: "try to rollback in case a test was failed and delete generated changelogs"
        if (shouldRunChangeSet) {
            try {
                argsMap.put("changeLogFile", testInput.xmlChangelogPath)
                strategy.performRollback(argsMap)
            } catch (CommandExecutionException exception) {
                //Ignore exception considering a test was successful
            }
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.key.equalsIgnoreCase("expectedSqlChangelog")) {
                deleteFile(testResourcesPath + sqlSpecificChangelogFile)
            } else {
                deleteFile(testResourcesPath + entry.value)
            }
        }

        where: "test input in next data table"
        testInput << buildTestInput()
    }

//    @Unroll
//    def "apply stress test against #testInput.databaseName #testInput.version"() {
//        given: "read input data for stress testing"
//        Map<String, Object> argsMap = new HashMap()
//        argsMap.put("url", testInput.url)
//        argsMap.put("username", testInput.username)
//        argsMap.put("password", testInput.password)
//        boolean shouldRunChangeSet
//
//        and: "check database under test is online"
//        def connection = testInput.database.getConnection()
//        shouldRunChangeSet = connection instanceof JdbcConnection
//        assert shouldRunChangeSet: "Database ${testInput.databaseName} ${testInput.version} is offline!"
//
//        and: "executing stress test with queries for 10000 rows"
//        def map = new LinkedHashMap<String, String>()
//        map.put("setup", testInput.setupChangelogPath)
//        map.put("insert", testInput.insertChangelogPath)
//        map.put("update", testInput.updateChangelogPath)
//        map.put("select", testInput.selectChangelogPath)
//        for (Map.Entry<String, String> entry : map.entrySet()) {
//            timeMillisBeforeTest = System.currentTimeMillis()
//            uiService.sendMessage("Executing $entry.key query: 10000 rows!")
//            argsMap.put("changeLogFile", entry.value)
//            executeCommandScope("update", argsMap)
//            timeMillisAfterTest = System.currentTimeMillis()
//            uiService.sendMessage("Execution time for $entry.key query: " + (timeMillisAfterTest - timeMillisBeforeTest) / 1000 + "s")
//        }
//
//        cleanup: "rollback changes if we ran changeSet"
//        if (shouldRunChangeSet) {
//            argsMap.put("changeLogFile", testInput.setupChangelogPath)
//            strategy.performRollback(argsMap)
//        }
//
//        where: "test input in next data table"
//        testInput << buildTestInput()
//    }

    def cleanupSpec() {
        strategy.cleanupDatabase(databases)
    }
}
