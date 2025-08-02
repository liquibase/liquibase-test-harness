package liquibase.harness.snapshot

import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.rollback.RollbackStrategy
import liquibase.harness.lifecycle.TestLifecycleManager
import liquibase.harness.lifecycle.TestContext
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
    @Shared
    TestLifecycleManager lifecycleManager = TestLifecycleManager.getInstance()

    def setupSpec() {
        databases = TestConfig.instance.getFilteredDatabasesUnderTest()
        strategy = chooseRollbackStrategy()
        strategy.prepareForRollback(databases)
    }

    @Unroll
    def "apply #testInput.snapshotObject against #testInput.databaseName #testInput.databaseVersion"() {
        given: "setup lifecycle context and read input data for snapshot command test"
        DatabaseUnderTest databaseUnderTest = databases.find { it.name == testInput.databaseName && it.version == testInput.databaseVersion }
        TestContext lifecycleContext = databaseUnderTest ? new TestContext(
            databaseUnderTest,
            this.class.simpleName,
            testInput.snapshotObject
        ) : null
        
        // Execute pre-test lifecycle hooks if database found
        if (lifecycleContext) {
            lifecycleManager.beforeTest(lifecycleContext)
        }
        
        and: "read input data for snapshot command test"
        Map<String, Object> argsMap = new HashMap()
        
        // Use isolated schema in URL if schema isolation is active
        String testUrl = testInput.url
        if (lifecycleContext && lifecycleContext.getMetadata("testSchema")) {
            String isolatedSchema = lifecycleContext.getMetadata("testSchema")
            // Replace schema in URL with isolated test schema
            testUrl = testInput.url.replaceAll(/schema=\w+/, "schema=${isolatedSchema}")
        }
        
        argsMap.put("url", testUrl)
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

        cleanup: "rollback changes and execute post-test lifecycle hooks"
        if (shouldRunChangeSet) {
            strategy.performRollback(argsMap)
        }
        
        // Execute post-test lifecycle hooks if database context exists
        if (lifecycleContext) {
            lifecycleManager.afterTest(lifecycleContext)
        }

        where:
        testInput << buildTestInput()
    }

    def cleanupSpec() {
        strategy.cleanupDatabase(databases)
    }
}
