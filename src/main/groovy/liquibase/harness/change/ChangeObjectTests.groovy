package liquibase.harness.change

import liquibase.Scope
import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.rollback.RollbackStrategy
import org.junit.jupiter.api.Assumptions
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.harness.util.FileUtils.*
import static liquibase.harness.util.SnapshotHelpers.snapshotMatchesSpecifiedStructure
import static liquibase.harness.util.TestUtils.*
import static liquibase.harness.change.ChangeObjectTestHelper.*

class ChangeObjectTests extends Specification {
    @Shared
    RollbackStrategy strategy
    @Shared
    List<DatabaseUnderTest> databases

    def setupSpec() {
        databases = TestConfig.instance.getFilteredDatabasesUnderTest()
        strategy = chooseRollbackStrategy()
        strategy.prepareForRollback(databases)
    }

    @Unroll
    def "apply #testInput.changeObject against #testInput.databaseName #testInput.version"() {
        given: "read expected sql and snapshot files, create arguments map for executing command scope"
        String rawExpectedSql = getSqlFileContent(testInput.changeObject, testInput.databaseName, testInput.version, "liquibase/harness/change/expectedSql")
        String expectedSql = parseQuery(substitutePlaceholders(rawExpectedSql, testInput.getDatabaseName(), testInput.getDbSchema()))
        String expectedSnapshot = getJSONFileContent(testInput.changeObject, testInput.databaseName, testInput.version,
                "liquibase/harness/change/expectedSnapshot")
        boolean shouldRunChangeSet
        Map<String, Object> argsMap = new HashMap()
        argsMap.put("changeLogFile", testInput.pathToChangeLogFile)
        argsMap.put("url", testInput.url)
        argsMap.put("username", testInput.username)
        argsMap.put("password", testInput.password)
        argsMap.put("snapshotFormat", "JSON")

        and: "ignore testcase if it's invalid or skipped for this combination of db type and/or version"
        def lowerExpectedSql = expectedSql?.toLowerCase()
        shouldRunChangeSet = !lowerExpectedSql?.contains("invalid test") && !lowerExpectedSql?.contains("skip test")
        Assumptions.assumeTrue(shouldRunChangeSet, expectedSql)

        and: "fail test if snapshot is not provided"
        shouldRunChangeSet = expectedSnapshot != null
        assert shouldRunChangeSet: "No expectedSnapshot for ${testInput.changeObject}!"

        and: "check database under test is online"
        shouldRunChangeSet = testInput.database.getConnection() instanceof JdbcConnection
        assert shouldRunChangeSet: "Database ${testInput.databaseName} ${testInput.version} is offline!"

        when: "get sql generated for the change set"
        def generatedSql = parseQuery(executeCommandScope("updateSql", argsMap).toString())

        then: "verify expected sql matches generated sql"
        if (expectedSql != null && !testInput.pathToChangeLogFile.endsWith(".sql")) {
            shouldRunChangeSet = generatedSql == expectedSql
            if (!shouldRunChangeSet) {
                Scope.getCurrentScope().getUI().sendMessage("FAIL! Expected sql doesn't " +
                        "match generated sql! Deleting expectedSql file will test that new sql works correctly and " +
                        "will auto-generate a new version if it passes. \nEXPECTED SQL: \n" + expectedSql + " \n" +
                        "GENERATED SQL: \n" + generatedSql)
                assert generatedSql == expectedSql
            }
            if (!TestConfig.instance.revalidateSql) {
                return //sql is right. Nothing more to test
            }
        }

        when: "apply changeSet to DB"
        executeCommandScope("update", argsMap)

        then: "get DB snapshot, check if actual snapshot matches expected snapshot"
        def generatedSnapshot = executeCommandScope("snapshot", argsMap).toString()
        snapshotMatchesSpecifiedStructure(expectedSnapshot, generatedSnapshot)

        and: "if expected sql is not provided save generated sql as expected sql"
        if (expectedSql == null && generatedSql != null && !testInput.pathToChangeLogFile.endsWith(".sql") && !generatedSql.isEmpty()) {
            saveAsExpectedSql(generatedSql, testInput)
        }

        cleanup: "rollback changes if we ran changeSet"
        if (shouldRunChangeSet) {
            strategy.performRollback(argsMap)
        }

        where: "test input in next data table"
        testInput << buildTestInput()
    }

    def cleanupSpec() {
        strategy.cleanupDatabase(databases)
    }
}
