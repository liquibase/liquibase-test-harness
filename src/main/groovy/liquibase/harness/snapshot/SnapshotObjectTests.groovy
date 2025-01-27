package liquibase.harness.snapshot

import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.rollback.RollbackStrategy
import org.junit.jupiter.api.Assumptions
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.harness.util.SnapshotHelpers.snapshotMatchesSpecifiedStructure
import static liquibase.harness.util.FileUtils.getResourceContent
import static liquibase.harness.util.TestUtils.chooseRollbackStrategy
import static liquibase.harness.util.TestUtils.executeCommandScope
import static liquibase.harness.snapshot.SnapshotObjectTestHelper.*

class SnapshotObjectTests extends Specification {

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
    def "apply #testInput.snapshotObject against #testInput.databaseName #testInput.databaseVersion"() {
        given: "read input data for snapshot command test"
        Map<String, Object> argsMap = new HashMap()
        argsMap.put("url", testInput.url)
        argsMap.put("username", testInput.username)
        argsMap.put("password", testInput.password)
        argsMap.put("changeLogFile", testInput.pathToChangelogFile)
        argsMap.put("snapshotFormat", "json")
        boolean shouldRunChangeSet
        String expectedSnapshot = getResourceContent(testInput.pathToExpectedSnapshotFile)

        and: "check database under test is online"
        def connection = testInput.database.getConnection()
        shouldRunChangeSet = connection instanceof JdbcConnection
        assert shouldRunChangeSet: "Database ${testInput.databaseName} ${testInput.databaseVersion} is offline!"

        and: "ignore testcase if it's invalid for this combination of db type and/or version"
        shouldRunChangeSet = !expectedSnapshot?.toLowerCase()?.contains("invalid test")
        Assumptions.assumeTrue(shouldRunChangeSet, expectedSnapshot)

        and: "check expected snapshot file is present"
        assert expectedSnapshot != null : "No expectedSnapshot for ${testInput.snapshotObject} against " +
                "${testInput.databaseName}${testInput.databaseVersion}"

        when: "execute update and generate snapshot"
        executeCommandScope("update", argsMap)
        def generatedSnapshot = executeCommandScope("snapshot", argsMap).toString()

        then: "compare generated to expected snapshot"
        snapshotMatchesSpecifiedStructure(expectedSnapshot, generatedSnapshot)

        cleanup: "rollback changes"
        if (shouldRunChangeSet) {
            strategy.performRollback(argsMap)
        }

        where:
        testInput << buildTestInput()
    }

    def cleanupSpec() {
        strategy.cleanupDatabase(databases)
    }
}
