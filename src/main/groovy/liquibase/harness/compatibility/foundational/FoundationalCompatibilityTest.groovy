package liquibase.harness.compatibility.foundational

import liquibase.Scope
import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.rollback.RollbackStrategy
import liquibase.ui.UIService
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.harness.compatibility.foundational.FoundationalCompatibilityTestHelper.buildTestInput
import static liquibase.harness.util.TestUtils.chooseRollbackStrategy
import static liquibase.harness.util.TestUtils.executeCommandScope

@Unroll
class FoundationalCompatibilityTest extends Specification {
    @Shared
    RollbackStrategy strategy;
    @Shared
    List<DatabaseUnderTest> databases
    long timeMillisBeforeTest
    long timeMillisAfterTest

    def setupSpec() {
        databases = TestConfig.instance.getFilteredDatabasesUnderTest()
        strategy = chooseRollbackStrategy()
        strategy.prepareForRollback(databases)
    }

    def "apply #testInput.change against #testInput.databaseName #testInput.version"() {
        given: "read input data"
        UIService uiService = Scope.getCurrentScope().getUI()

        Map<String, Object> argsMap = new HashMap()
        argsMap.put("url", testInput.url)
        argsMap.put("username", testInput.username)
        argsMap.put("password", testInput.password)

        boolean shouldRunChangeSet

        and: "check database under test is online"
        def connection = testInput.database.getConnection()
        shouldRunChangeSet = connection instanceof JdbcConnection
        assert shouldRunChangeSet: "Database ${testInput.databaseName} ${testInput.version} is offline!"

        and: "create test table"
        timeMillisBeforeTest = System.currentTimeMillis()
        uiService.sendMessage("Executing setup query!")
        argsMap.put("changeLogFile", testInput.setupChangelogPath)
        executeCommandScope("update", argsMap)
        timeMillisAfterTest = System.currentTimeMillis()
        uiService.sendMessage("Setup execution time: " + (timeMillisAfterTest - timeMillisBeforeTest)/1000 + "s")

        and: "execute big insert query"
        timeMillisBeforeTest = System.currentTimeMillis()
        uiService.sendMessage("Executing insert query: 10000 rows!")
        argsMap.put("changeLogFile", testInput.insertChangelogPath)
        executeCommandScope("update", argsMap)
        timeMillisAfterTest = System.currentTimeMillis()
        uiService.sendMessage("Insert query execution time: " + (timeMillisAfterTest - timeMillisBeforeTest)/1000 + "s")

        and: "execute update query"
        timeMillisBeforeTest = System.currentTimeMillis()
        uiService.sendMessage("Executing update query: 10000 rows!")
        argsMap.put("changeLogFile", testInput.updateChangelogPath)
        executeCommandScope("update", argsMap)
        timeMillisAfterTest = System.currentTimeMillis()
        uiService.sendMessage("Update query execution time: " + (timeMillisAfterTest - timeMillisBeforeTest)/1000 + "s")

        and: "execute select query"
        timeMillisBeforeTest = System.currentTimeMillis()
        uiService.sendMessage("Executing select query: 10000 rows!")
        argsMap.put("changeLogFile", testInput.selectChangelogPath)
        executeCommandScope("update", argsMap)
        timeMillisAfterTest = System.currentTimeMillis()
        uiService.sendMessage("Select query execution time: " + (timeMillisAfterTest - timeMillisBeforeTest)/1000 + "s")

        cleanup: "rollback changes if we ran changeSet"
        if (shouldRunChangeSet) {
            argsMap.put("changeLogFile", testInput.setupChangelogPath)
            strategy.performRollback(argsMap)
        }

        where: "test input in next data table"
        testInput << buildTestInput()
    }

    def cleanupSpec() {
        strategy.cleanupDatabase(databases)
    }
}
