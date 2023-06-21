package liquibase.harness.compatibility.advanced

import liquibase.Scope
import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.rollback.RollbackStrategy
import liquibase.resource.SearchPathResourceAccessor
import org.apache.commons.io.FileUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Path
import java.nio.file.Paths

import static liquibase.harness.compatibility.advanced.AdvancedTestHelper.*
import static liquibase.harness.util.FileUtils.*
import static liquibase.harness.util.SnapshotHelpers.snapshotMatchesSpecifiedStructure
import static liquibase.harness.util.TestUtils.*

class AdvancedTest extends Specification {
    @Shared
    RollbackStrategy strategy;
    @Shared
    List<DatabaseUnderTest> databases;
    String inputResourcesDirFullPath = System.getProperty("user.dir") + "/src/main/resources/"
    String outputResourcesDirFullPath = System.getProperty("user.dir") + "/target/test-classes/"
    String outputResourcesDirPath = "/target/test-classes/"

    def setupSpec() {
        databases = TestConfig.instance.getFilteredDatabasesUnderTest()
        strategy = chooseRollbackStrategy()
        strategy.prepareForRollback(databases)
    }

    @Unroll
    def "apply end2end test for #testInput.change against #testInput.databaseName #testInput.version"() {
        given: "read input data for advanced test"

        Map<String, Object> argsMapPrimary = new HashMap()
        argsMapPrimary.put("url", testInput.url)
        argsMapPrimary.put("username", testInput.username)
        argsMapPrimary.put("password", testInput.password)
        argsMapPrimary.put("sqlFile", inputResourcesDirFullPath + testInput.primaryInitSqlPath)
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

        and: "configuring test data for generateChangelog command for all files format"
        def shortDbName = getShortDatabaseName(testInput.databaseName)
        def generateChangelogMap = configureChangelogMap(testInput.generateChangelogResourcesPath, shortDbName)
        def diffChangelogMap = configureChangelogMap(testInput.diffChangelogResourcesPath, shortDbName)

        when: "apply init script to DB"
        executeCommandScope("executeSql", argsMapPrimary)

        then: "get DB snapshot, check if actual snapshot matches expected snapshot"
        def generatedSnapshot = executeCommandScope("snapshot", argsMapPrimary).toString()
        snapshotMatchesSpecifiedStructure(testInput.expectedSnapshot, generatedSnapshot)

        for (Map.Entry<String, String> entry : generateChangelogMap.entrySet()) {

            when: "clean 'objects' directory if created"
            String testResourcesPath = outputResourcesDirFullPath + "liquibase/harness/compatibility/advanced/generatedChangelogs/objects/"
            Path path = Paths.get(testResourcesPath)
            if (path.toFile().isDirectory()) {
                FileUtils.forceDelete(new File(testResourcesPath))
            }

            and: "execute generateChangelog command using different changelog formats"
            argsMapPrimary.put("changelogFile", outputResourcesDirFullPath + entry.value)
            executeCommandScope("generateChangelog", argsMapPrimary)

            then: "verify changelog was actually generated and validate it's content"
            String generatedChangelog = parseQuery(readFile((String) argsMapPrimary.get("changelogFile")))
            validateGenerateChangelog(entry.key, generatedChangelog, testInput.expectedGenerateChangelogSql, testInput.change, "generateChangelog")

            when: "execute updateSql command on generated changelogs"
            argsMapPrimary.put("changelogFile", outputResourcesDirPath + entry.value)
            def generatedSql = parseQuery(executeCommandScope("updateSql", argsMapPrimary).toString())
            generatedSql = removeSchemaNames(generatedSql, testInput.database, testInput.primaryDbSchemaName)
            def expectedSql = testInput.expectedGenerateChangelogSql

            then: "validate generated sql"
            if (expectedSql != null) {
                validateSql(generatedSql, expectedSql)
            } else {
                saveAsExpectedSql(generatedSql, testInput, "generateChangelog")
            }
        }

        when: "execute diff command"
        String generatedDiff = removeDatabaseInfoFromDiff(executeCommandScope("diff", argsMapSecondary).toString())

        then: "validate generated diff"
        String expectedDiff = removeDatabaseInfoFromDiff(getResourceContent("/$testInput.pathToExpectedDiffFile"))
        validateDiff(generatedDiff, expectedDiff)

        when: "apply generated changelog to secondary database instance and execute diff command"
        argsMapSecondary.put("changelogFile", outputResourcesDirPath + generateChangelogMap.get("jsonChangelog"))
        Scope.getCurrentScope().getUI().sendMessage("APPLY GENERATED CHANGELOG TO SECONDARY DATABASE")
        executeCommandScope("update", argsMapSecondary)
        generatedDiff = removeDatabaseInfoFromDiff(executeCommandScope("diff", argsMapSecondary).toString())
        expectedDiff = removeDatabaseInfoFromDiff(getResourceContent("/$testInput.pathToEmptyDiffFile"))

        then: "validate diff command shows no differences"
        validateDiff(generatedDiff, expectedDiff)

        when: "clear secondary instance"
        argsMapSecondary.remove("changelogFile")
        Scope.getCurrentScope().getUI().sendMessage("CLEAN SECONDARY DATABASE INSTANCE")
        executeCommandScope("dropAll", argsMapSecondary)
        argsMapSecondary.put("sqlFile", inputResourcesDirFullPath + testInput.secondaryInitSqlPath)

        then: "apply init script to secondary instance"
        Scope.getCurrentScope().getUI().sendMessage("SETUP SECONDARY DATABASE INSTANCE FOR DIFFCHANGELOG COMMAND")
        executeCommandScope("executeSql", argsMapSecondary)

        for (Map.Entry<String, String> entry : diffChangelogMap.entrySet()) {

            when: "execute diffChangelog command using different changelog formats"
            argsMapSecondary.put("changelogFile", outputResourcesDirFullPath + entry.value)
            executeCommandScope("diffChangelog", argsMapSecondary)

            then: "verify diff changelog was actually generated and validate it's content"
            String diffChangelog = parseQuery(readFile((String) argsMapSecondary.get("changelogFile")))
            validateDiffChangelog(entry.key, diffChangelog, testInput.expectedDiffChangelogSql, testInput.change, testInput.changeReversed, "diffChangelog")

            when: "execute updateSql command on generated changelogs"
            argsMapSecondary.put("changelogFile", outputResourcesDirPath + entry.value)
            def generatedSql = parseQuery(executeCommandScope("updateSql", argsMapSecondary).toString())
            generatedSql = removeSchemaNames(generatedSql, testInput.database, testInput.secondaryDbSchemaName)
            def expectedSql = testInput.expectedDiffChangelogSql

            then: "validate generated sql"
            if (expectedSql != null) {
                validateSql(generatedSql, expectedSql)
            } else {
                saveAsExpectedSql(generatedSql, testInput, "diffChangelog")
            }
        }

        when: "apply generated diffChangelog to secondary database instance and execute diff command"
        argsMapSecondary.put("changelogFile", outputResourcesDirPath + diffChangelogMap.get("xmlChangelog"))
        Scope.getCurrentScope().getUI().sendMessage("APPLY GENERATED DIFFCHANGELOG TO SECONDARY DATABASE")
        executeCommandScope("update", argsMapSecondary)
        generatedDiff = removeDatabaseInfoFromDiff(executeCommandScope("diff", argsMapSecondary).toString())
        expectedDiff = removeDatabaseInfoFromDiff(getResourceContent("/$testInput.pathToEmptyDiffFile"))

        then: "validate diff command shows no differences"
        validateDiff(generatedDiff, expectedDiff)

        cleanup: "try to rollback in case a test was failed and delete generated changelogs"
        argsMapPrimary.remove("changelogFile")
        argsMapSecondary.remove("changelogFile")
        executeCommandScope("dropAll", argsMapPrimary)
        executeCommandScope("dropAll", argsMapSecondary)
        for (Map.Entry<String, String> entry : generateChangelogMap.entrySet()) {
            deleteFile(outputResourcesDirFullPath + entry.value)
        }
        for (Map.Entry<String, String> entry : diffChangelogMap.entrySet()) {
            deleteFile(outputResourcesDirFullPath + entry.value)
        }

        where: "test input in next data table"
        testInput << buildTestInput()
    }

    def cleanupSpec() {
        strategy.cleanupDatabase(databases)
    }
}
