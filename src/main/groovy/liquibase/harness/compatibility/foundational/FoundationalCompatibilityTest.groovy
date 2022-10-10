package liquibase.harness.compatibility.foundational

import liquibase.Scope
import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.rollback.RollbackStrategy
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
        Map<String, Object> argsMap = new HashMap()
        argsMap.put("url", testInput.url)
        argsMap.put("username", testInput.username)
        argsMap.put("password", testInput.password)

        String setupChangelog = testInput.baseChangelogPath + "setup/" + testInput.change + ".xml"
        String insertChangelog = testInput.baseChangelogPath + "insert/" + testInput.change + ".xml"
        String updateChangelog = testInput.baseChangelogPath + "update/" + testInput.change + ".xml"
        String selectChangelog = testInput.baseChangelogPath + "select/" + testInput.change + ".xml"


        boolean shouldRunChangeSet

        and: "check database under test is online"
        def connection = testInput.database.getConnection()
        shouldRunChangeSet = connection instanceof JdbcConnection
        assert shouldRunChangeSet: "Database ${testInput.databaseName} ${testInput.version} is offline!"

        and: "create test table"
        timeMillisBeforeTest = System.currentTimeMillis()
        Scope.getCurrentScope().getUI().sendMessage("Executing setup query!")
        argsMap.put("changeLogFile", setupChangelog)
        executeCommandScope("update", argsMap)
        timeMillisAfterTest = System.currentTimeMillis()
        Scope.getCurrentScope().getUI().sendMessage("Setup execution time: " + (timeMillisAfterTest - timeMillisBeforeTest)/1000 + "s")

        and: "execute big insert query"
        timeMillisBeforeTest = System.currentTimeMillis()
        Scope.getCurrentScope().getUI().sendMessage("Executing insert query: 100000 rows!")
        argsMap.put("changeLogFile", insertChangelog)
        executeCommandScope("update", argsMap)
        timeMillisAfterTest = System.currentTimeMillis()
        Scope.getCurrentScope().getUI().sendMessage("Insert query execution time: " + (timeMillisAfterTest - timeMillisBeforeTest)/1000 + "s")

        and: "execute update query"
        timeMillisBeforeTest = System.currentTimeMillis()
        Scope.getCurrentScope().getUI().sendMessage("Executing update query: 100000 rows!")
        argsMap.put("changeLogFile", updateChangelog)
        executeCommandScope("update", argsMap)
        timeMillisAfterTest = System.currentTimeMillis()
        Scope.getCurrentScope().getUI().sendMessage("Update query execution time: " + (timeMillisAfterTest - timeMillisBeforeTest)/1000 + "s")

        and: "execute select query"
        timeMillisBeforeTest = System.currentTimeMillis()
        Scope.getCurrentScope().getUI().sendMessage("Executing select query: 100000 rows!")
        argsMap.put("changeLogFile", selectChangelog)
        executeCommandScope("update", argsMap)
        timeMillisAfterTest = System.currentTimeMillis()
        Scope.getCurrentScope().getUI().sendMessage("Select query execution time: " + (timeMillisAfterTest - timeMillisBeforeTest)/1000 + "s")

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
