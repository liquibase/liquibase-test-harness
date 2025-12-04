package liquibase.harness.generateChangelog

import liquibase.database.jvm.JdbcConnection
import liquibase.exception.CommandExecutionException
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.rollback.RollbackStrategy
import org.junit.jupiter.api.Assumptions
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Paths

import static liquibase.harness.generateChangelog.GenerateChangelogTestHelper.*
import static liquibase.harness.util.FileUtils.getResourceContent
import static liquibase.harness.util.FileUtils.readFile
import static liquibase.harness.util.TestUtils.chooseRollbackStrategy
import static liquibase.harness.util.TestUtils.executeCommandScope

class GenerateChangelogTest extends Specification {
    @Shared
    RollbackStrategy strategy
    @Shared
    List<DatabaseUnderTest> databases
    @Shared
    String resourcesDirPath = "src/test/resources/"

    def setupSpec() {
        databases = TestConfig.instance.getFilteredDatabasesUnderTest()
        strategy = chooseRollbackStrategy()
        strategy.prepareForRollback(databases)
    }

    @Unroll
    def "apply generateChangelog test for #testInput.change against #testInput.databaseName #testInput.version"() {
        given: "read input data for generateChangelog test"
        String expectedSql = getResourceContent("/$testInput.expectedSqlPath")
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
        shouldRunChangeSet = !expectedSql.toLowerCase()?.contains("invalid test")
        Assumptions.assumeTrue(shouldRunChangeSet, expectedSql)

        when: "execute update command using xml changelog formats"
        argsMap.put("changeLogFile", testInput.inputChangelogFile)
        executeCommandScope("update", argsMap)

        and: "testing generateChangelog command for all files format"
        String generatedFolderPath = Paths.get(resourcesDirPath, baseChangelogPath, "generated").toString()
        String generatedChangeTypePath = Paths.get(resourcesDirPath, baseChangelogPath, "generated", testInput.databaseName, testInput.change).toString()
        def formats = new LinkedHashMap<String, String>()
        def shortDbName = getShortDatabaseName(testInput.databaseName)
        formats.put("XmlTestCase", generatedChangeTypePath + ".xml")
//        formats.put("SqlTestCase", generatedChangeTypePath + ".$shortDbName"+".sql")  //TODO commented until DAT-**** is done
        formats.put("YmlTestCase",  generatedChangeTypePath + ".yml")
        formats.put("JsonTestCase", generatedChangeTypePath + ".json")

        then: "check if a changelog was actually generated and validate it's content"
        for (Map.Entry<String, String> entry : formats.entrySet()) {

            if (entry.key.equalsIgnoreCase("SqlTestCase")) {
                argsMap.put("generateInlineSql", true)
            } else {
                argsMap.put("generateInlineSql", false)
            }
            clearFolder(generatedFolderPath)

            argsMap.put("excludeObjects", "(?i)posts, (?i)authors")//excluding static test-harness objects from generated changelog
            argsMap.put("changeLogFile", entry.value)
            executeCommandScope("generateChangelog", argsMap)

            String generatedChangelog = readFile((String) argsMap.get("changeLogFile"))
            if (entry.key.equalsIgnoreCase("SqlTestCase")) {
                validateSqlChangelog(expectedSql, generatedChangelog)
            } else {
                and: "verify that the 'stored objects' directories are created"
                def storedObjectTypesMap = [
                        //Should be fixed by DAT-19461
                        "createPackage"     : (shortDbName == "edb-edb" ? "databasepackage" : "package"),
                        "createPackageBody" :  (shortDbName == "edb-edb" ? "databasepackagebody" : "packagebody"),
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

    def cleanupSpec() {
        strategy.cleanupDatabase(databases)
    }
}
