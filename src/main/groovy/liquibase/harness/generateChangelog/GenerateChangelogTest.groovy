package liquibase.harness.generateChangelog

import com.datical.liquibase.ext.config.LiquibaseProConfiguration
import liquibase.Scope
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.CommandExecutionException
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.rollback.RollbackStrategy
import liquibase.ui.UIService
import org.junit.jupiter.api.Assumptions
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Paths

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
    String resourcesDirPath = "src/test/resources/"
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
        shouldRunChangeSet = !getResourceContent("/$testInput.expectedSqlPath").toLowerCase()?.contains("invalid test")
        Assumptions.assumeTrue(shouldRunChangeSet, "INFO: Test for $testInput.change is ignored")

        when: "execute update command using xml changelog formats"
        argsMap.put("changeLogFile", testInput.inputChangelogFile)
        executeCommandScope("update", argsMap)

        and: "testing generateChangelog command for all files format"
        String generatedFolderPath = Paths.get(resourcesDirPath, baseChangelogPath, "generated").toString()
        String generatedChangeTypePath = Paths.get(resourcesDirPath, baseChangelogPath, "generated", testInput.databaseName, testInput.change).toString()
        def formats = new LinkedHashMap<String, String>()
        def shortDbName = getShortDatabaseName(testInput.databaseName)
        formats.put("XmlTestCase", generatedChangeTypePath + ".xml")
        formats.put("SqlTestCase", generatedChangeTypePath + ".$shortDbName"+".sql")
        formats.put("YmlTestCase",  generatedChangeTypePath + ".yml")
        formats.put("JsonTestCase", generatedChangeTypePath + ".json")

        then: "check if a changelog was actually generated and validate it's content"
        for (Map.Entry<String, String> entry : formats.entrySet()) {

            Map<String, Object> scopeValues = new HashMap<>()
            if (entry.key.equalsIgnoreCase("SqlTestCase")) {
                scopeValues.put(LiquibaseProConfiguration.INLINE_SQL_KEY.getKey(), true)
            } else {
                scopeValues.put(LiquibaseProConfiguration.INLINE_SQL_KEY.getKey(), false)
            }

            clearFolder(generatedFolderPath)

            argsMap.put("excludeObjects", "(?i)posts, (?i)authors")//excluding static test-harness objects from generated changelog
            argsMap.put("changeLogFile", entry.value)
            executeCommandScope("generateChangelog", argsMap, scopeValues)

            String generatedChangelog = readFile((String) argsMap.get("changeLogFile"))
            if (entry.key.equalsIgnoreCase("SqlTestCase")) {
                validateSqlChangelog(getResourceContent("/$testInput.expectedSqlPath"), generatedChangelog)
            } else {
                and: "verify that the 'stored objects' directories are created"
                def storedObjectTypesMap = [
                        "createPackage"     : "package",
                        "createPackageBody" : "packagebody",
                        "createFunction"    : "function",
                        "createProcedure"   : "storedprocedure",
                        "createTrigger"     : "trigger"
                ]

                if (storedObjectTypesMap.keySet().any { changelogType -> testInput.change.equalsIgnoreCase(changelogType) }) {
                    def expectedObjectType = storedObjectTypesMap[testInput.change]

                    def originalPath = entry.value
                    def replacedPath = originalPath.replaceAll(/create\w+\.(xml|yml|json)$/, "") + "objects/" + expectedObjectType

                    def objectDir = new File(replacedPath)
                    assert objectDir.exists() && objectDir.isDirectory() :
                            "Directory for stored object '${expectedObjectType}' was not created at path: ${replacedPath}!"
                }
            }

//TODO will be fixed in DAT-14675.
/*
            when: "get sql generated for the change set"
            String generatedSql
            argsMap.put("changeLogFile", resourcesDirFullPath + entry.value)
            if (!entry.key.equalsIgnoreCase("SqlTestCase")) {
                generatedSql = parseQuery(executeCommandScope("updateSql", argsMap).toString())
                generatedSql = removeSchemaNames(generatedSql, testInput.database)
            }

            then: "execute updateSql command on generated changelogs"
            if (!entry.key.equalsIgnoreCase("SqlTestCase")) {
                def expectedSql
                try {
                    expectedSql = parseQuery(getSqlFileContent(testInput.change, testInput.databaseName, testInput.version,
                            "liquibase/harness/generateChangelog/verificationSql")).toLowerCase()
                } catch (NullPointerException exception) {
                    expectedSql = parseQuery(getSqlFileContent(testInput.change, testInput.databaseName, testInput.version,
                            "liquibase/harness/generateChangelog/expectedSql")).toLowerCase()
                }
                def generatedSqlIsCorrect = generatedSql == expectedSql
                if (!generatedSqlIsCorrect) {
                    Scope.getCurrentScope().getUI().sendMessage("FAIL! Expected sql doesn't " +
                            "match generated sql! \nEXPECTED SQL: \n" + expectedSql + " \n" +
                            "GENERATED SQL: \n" + generatedSql)
                    assert generatedSql == expectedSql
                }
            }
 */

        }

        cleanup: "try to rollback in case a test was failed and delete generated changelogs"
        if (shouldRunChangeSet) {
            try {
                argsMap.put("changeLogFile", testInput.inputChangelogFile)
                strategy.performRollback(argsMap)
            } catch (CommandExecutionException exception) {
                //Ignore exception considering a test was successful
            }
        }
        clearFolder(generatedFolderPath)

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
