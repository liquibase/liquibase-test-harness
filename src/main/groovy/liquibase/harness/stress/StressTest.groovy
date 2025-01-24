package liquibase.harness.stress

import liquibase.Scope
import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.rollback.RollbackStrategy
import liquibase.ui.UIService
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.harness.util.TestUtils.chooseRollbackStrategy
import static liquibase.harness.util.TestUtils.executeCommandScope

class StressTest extends Specification {
    @Shared
    RollbackStrategy strategy
    @Shared
    List<DatabaseUnderTest> databases
    @Shared
    UIService uiService = Scope.getCurrentScope().getUI()

    long timeMillisBeforeTest
    long timeMillisAfterTest

    def setupSpec() {
        databases = TestConfig.instance.getFilteredDatabasesUnderTest()
        strategy = chooseRollbackStrategy()
        strategy.prepareForRollback(databases)
    }

    @Unroll
    def "apply stress test against #testInput.databaseName #testInput.version"() {
        given: "read input data for stress testing"
        Map<String, Object> argsMap = new HashMap()
        argsMap.put("url", testInput.url)
        argsMap.put("username", testInput.username)
        argsMap.put("password", testInput.password)
        boolean shouldRunChangeSet

        and: "check database under test is online"
        def connection = testInput.database.getConnection()
        shouldRunChangeSet = connection instanceof JdbcConnection
        assert shouldRunChangeSet: "Database ${testInput.databaseName} ${testInput.version} is offline!"

        and: "executing stress test with queries for 10000 rows"
        def map = new LinkedHashMap<String, String>()
        map.put("setup", testInput.setupChangelogPath)
        map.put("insert", testInput.insertChangelogPath)
        map.put("update", testInput.updateChangelogPath)
        map.put("select", testInput.selectChangelogPath)
        for (Map.Entry<String, String> entry : map.entrySet()) {
            timeMillisBeforeTest = System.currentTimeMillis()
            uiService.sendMessage("Executing $entry.key query: 10000 rows!")
            argsMap.put("changeLogFile", entry.value)
            executeCommandScope("update", argsMap)
            timeMillisAfterTest = System.currentTimeMillis()
            uiService.sendMessage("Execution time for $entry.key query: " + (timeMillisAfterTest - timeMillisBeforeTest) / 1000 + "s")
        }

        cleanup: "rollback changes if we ran changeSet"
        if (shouldRunChangeSet) {
            argsMap.put("changeLogFile", testInput.setupChangelogPath)
            strategy.performRollback(argsMap)
        }

        where: "test input in next data table"
        testInput << StressTestHelper.buildTestInput()
    }

    def cleanupSpec() {
        strategy.cleanupDatabase(databases)
    }
}
