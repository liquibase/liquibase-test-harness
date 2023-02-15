package liquibase.harness.compatibility.advanced

import liquibase.Scope
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.CommandExecutionException
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.rollback.RollbackStrategy
import org.junit.Assume
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.harness.compatibility.advanced.AdvancedTestHelper.*
import static liquibase.harness.util.FileUtils.*
import static liquibase.harness.util.SnapshotHelpers.snapshotMatchesSpecifiedStructure
import static liquibase.harness.util.TestUtils.*

class AdvancedTest extends Specification{
    @Shared
    RollbackStrategy strategy;
    @Shared
    List<DatabaseUnderTest> databases;
    String resourcesDirFullPath = System.getProperty("user.dir") + "/src/test/resources/"
    String resourcesDirPath = "/src/test/resources/"

    def setupSpec() {
        databases = TestConfig.instance.getFilteredDatabasesUnderTest()
        strategy = chooseRollbackStrategy()
        strategy.prepareForRollback(databases)
    }

    @Unroll
    def "apply end2end test for #testInput.change against #testInput.databaseName #testInput.version"() {
        given: "read input data for advanced test"
        String expectedSql = parseQuery(getSqlFileContent(testInput.change, testInput.databaseName, testInput.version,
                "liquibase/harness/compatibility/advanced/expectedSql/setup"))

        Map<String, Object> argsMapPrimary = new HashMap()
        argsMapPrimary.put("url", testInput.url)
        argsMapPrimary.put("username", testInput.username)
        argsMapPrimary.put("password", testInput.password)
        argsMapPrimary.put("changelogFile", testInput.primarySetupChangelogPath)
        argsMapPrimary.put("snapshotFormat", "json")
        argsMapPrimary.put("excludeObjects", "(?i)posts, (?i)authors, (?i)databasechangelog, (?i)databasechangeloglock")//excluding static test-harness objects

        Map<String, Object> argsMapSecondary = new HashMap()
        argsMapSecondary.put("url", testInput.referenceUrl)
        argsMapSecondary.put("username", testInput.username)
        argsMapSecondary.put("password", testInput.password)
        argsMapSecondary.put("referenceUrl", testInput.url)
        argsMapSecondary.put("referenceUsername", testInput.username)
        argsMapSecondary.put("referencePassword", testInput.password)
        argsMapSecondary.put("excludeObjects", "(?i)posts, (?i)authors, (?i)databasechangelog, (?i)databasechangeloglock")
        boolean shouldRunChangeSet

        and: "check database under test is online"
        def connection = testInput.database.getConnection()
        shouldRunChangeSet = connection instanceof JdbcConnection
        assert shouldRunChangeSet: "Database ${testInput.databaseName} ${testInput.version} is offline!"

        and: "ignore testcase if it's invalid for this combination of db type and/or version"
        shouldRunChangeSet = !expectedSql?.toLowerCase()?.contains("invalid test")
        Assume.assumeTrue(expectedSql, shouldRunChangeSet)

        and: "configuring test data for generateChangelog command for all files format"
        def shortDbName = getShortDatabaseName(testInput.databaseName)
        def generateChangelogMap = configureChangelogMap(testInput.generateCLResourcesPath, shortDbName)
        def diffChangelogMap = configureChangelogMap(testInput.diffCLResourcesPath, shortDbName)

        when: "get sql generated for the change set"
        def generatedSql = parseQuery(executeCommandScope("updateSql", argsMapPrimary).toString())

        then: "verify expected sql matches generated sql"
        validateSql(generatedSql, expectedSql)

        when: "apply changeSet to DB"
        executeCommandScope("update", argsMapPrimary)

        then: "get DB snapshot, check if actual snapshot matches expected snapshot"
        def generatedSnapshot = executeCommandScope("snapshot", argsMapPrimary).toString()
        snapshotMatchesSpecifiedStructure(testInput.expectedSnapshot, generatedSnapshot)

        for (Map.Entry<String, String> entry : generateChangelogMap.entrySet()) {

            when: "execute generateChangelog command using different changelog formats"
            argsMapPrimary.put("changelogFile", resourcesDirFullPath + entry.value)
            executeCommandScope("generateChangelog", argsMapPrimary)

            then: "check if a changelog was actually generated and validate it's content"
            String generatedChangelog = parseQuery(readFile((String) argsMapPrimary.get("changelogFile")))
            validateChangelog(entry.key, generatedChangelog, testInput.verificationGenCLSql, testInput.change, null)

            when: "execute updateSql command on generated changelogs"
            argsMapPrimary.put("changelogFile", resourcesDirPath + entry.value)
            generatedSql = parseQuery(executeCommandScope("updateSql", argsMapPrimary).toString())
            generatedSql = removeSchemaNames(generatedSql, testInput.database, testInput.primaryDbSchemaName)


            then: "execute updateSql command on generated changelogs"
            expectedSql = getChangelogValidationSql("generateChangelog", testInput.change, testInput.databaseName, testInput.version)
            validateSql(generatedSql, expectedSql)
        }

        when: "execute diff command"
        String generatedDiff = cleanDiff(executeCommandScope("diff", argsMapSecondary).toString())

        then: "validate generated diff"
        String expectedDiff = cleanDiff(getResourceContent("/$testInput.pathToExpectedDiffFile"))
        assert generatedDiff == expectedDiff

        when: "apply generated changelog to secondary database instance and execute diff command"
        argsMapSecondary.put("changelogFile", resourcesDirPath + generateChangelogMap.get("xmlChangelog"))
        executeCommandScope("update", argsMapSecondary)
        generatedDiff = cleanDiff(executeCommandScope("diff", argsMapSecondary).toString())
        expectedDiff = cleanDiff(getResourceContent("/$testInput.pathToEmptyDiffFile"))

        then: "validate diff command shows no differences"
        assert generatedDiff == expectedDiff

        when: "clear secondary instance"
        argsMapSecondary.remove("changelogFile")
        executeCommandScope("dropAll", argsMapSecondary)
        argsMapSecondary.put("changelogFile", testInput.secondarySetupChangelogPath)

        then: "apply specific test data to secondary instance"
        executeCommandScope("update", argsMapSecondary)

        for (Map.Entry<String, String> entry : diffChangelogMap.entrySet()) {

            when: "execute diffChangelog command using different changelog formats"
            argsMapSecondary.put("changelogFile", resourcesDirFullPath + entry.value)
            executeCommandScope("diffChangelog", argsMapSecondary)

            then: "check if a changelog was actually generated and validate it's content"
            String diffChangelog = parseQuery(readFile((String) argsMapSecondary.get("changelogFile")))
            validateChangelog(entry.key, diffChangelog, testInput.verificationDiffCLSql, testInput.change, testInput.changeReversed)

            when: "execute updateSql command on generated changelogs"
            argsMapSecondary.put("changelogFile", resourcesDirPath + entry.value)
            generatedSql = parseQuery(executeCommandScope("updateSql", argsMapSecondary).toString())
            generatedSql = removeSchemaNames(generatedSql, testInput.database, testInput.secondaryDbSchemaName)

            then: "execute updateSql command on generated changelogs"
            expectedSql = getChangelogValidationSql("diffChangelog", testInput.change, testInput.databaseName, testInput.version)
            validateSql(generatedSql, expectedSql)
        }

        when: "apply generated diffChangelog to secondary database instance and execute diff command"
        argsMapSecondary.put("changelogFile", resourcesDirPath + diffChangelogMap.get("xmlChangelog"))
        executeCommandScope("update", argsMapSecondary)
        generatedDiff = cleanDiff(executeCommandScope("diff", argsMapSecondary).toString())
        expectedDiff = cleanDiff(getResourceContent("/$testInput.pathToEmptyDiffFile"))

        then: "validate diff command shows no differences"
        assert generatedDiff == expectedDiff

        cleanup: "try to rollback in case a test was failed and delete generated changelogs"
        if (shouldRunChangeSet) {
            try {
                argsMapPrimary.put("changeLogFile", testInput.primarySetupChangelogPath)
                strategy.performRollback(argsMapPrimary)
            } catch (CommandExecutionException exception) {
                //Ignore exception considering a test was successful
            }
        }
        argsMapSecondary.remove("changelogFile")
        executeCommandScope("dropAll", argsMapSecondary)
        for (Map.Entry<String, String> entry : generateChangelogMap.entrySet()) {
            deleteFile(resourcesDirFullPath + entry.value)
        }
        for (Map.Entry<String, String> entry : diffChangelogMap.entrySet()) {
            deleteFile(resourcesDirFullPath + entry.value)
        }


        where: "test input in next data table"
        testInput << buildTestInput()
    }

    def cleanupSpec() {
        strategy.cleanupDatabase(databases)
    }
}
