package liquibase.harness.base

import liquibase.Scope
import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.rollback.RollbackStrategy
import org.json.JSONObject
import org.junit.Assert
import org.skyscreamer.jsonassert.JSONCompareMode
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import java.sql.SQLException
import java.sql.Time
import java.time.LocalDateTime

import static BaseCompatibilityTestHelper.buildTestInput
import static liquibase.harness.util.FileUtils.*
import static liquibase.harness.util.JSONUtils.*
import static liquibase.harness.util.TestUtils.*

@Unroll
class BaseCompatibilityTest extends Specification {
    @Shared
    RollbackStrategy strategy;
    @Shared
    List<DatabaseUnderTest> databases;

    def setupSpec() {
        databases = TestConfig.instance.getFilteredDatabasesUnderTest()
        strategy = chooseRollbackStrategy()
        strategy.prepareForRollback(databases)
    }

    def "apply #testInput.change against #testInput.databaseName #testInput.version"() {
        given: "read input data"
        String checkObjectIsPresentSql = getSqlFileContent(testInput.change, testInput.databaseName, testInput.version,
                "liquibase/harness/base/checkingSql")
        String expectedResultSet = getJSONFileContent(testInput.change, testInput.databaseName, testInput.version,
                "liquibase/harness/base/expectedResultSet")
        Map<String, Object> argsMap = new HashMap()
        argsMap.put("url", testInput.url)
        argsMap.put("username", testInput.username)
        argsMap.put("password", testInput.password)
        argsMap.put("changeLogFile", testInput.pathToChangeLogFile)

        boolean shouldRunChangeSet

        and: "fail test if checkObjectIsPresentSql is not provided"
        shouldRunChangeSet = checkObjectIsPresentSql != null
        assert shouldRunChangeSet: "No checkObjectIsPresentSql for ${testInput.change} against " +
                "${testInput.database.shortName} ${testInput.database.databaseMajorVersion}." +
                "${testInput.database.databaseMinorVersion}"

        and: "fail test if expectedResultSet is not provided"
        shouldRunChangeSet = expectedResultSet != null
        assert shouldRunChangeSet: "No expectedResultSet for ${testInput.change} against " +
                "${testInput.database.shortName} ${testInput.database.databaseMajorVersion}." +
                "${testInput.database.databaseMinorVersion}"

        and: "check database under test is online"
        def connection = testInput.database.getConnection()
        shouldRunChangeSet = connection instanceof JdbcConnection
        assert shouldRunChangeSet: "Database ${testInput.databaseName} ${testInput.version} is offline!"

        //and: "execute Liquibase validate command to ensure a chagelog is valid"
        //executeCommandScope("validate", argsMap) //Doesn't work :(

        when: "execute XML-inlined changelog using liquibase update command"
        executeCommandScope("update", argsMap)

        and: "execute Liquibase tag command"
        argsMap.put("tag", "test_tag")
        executeCommandScope("tag", argsMap)

        and: "execute Liquibase tag command"
        executeCommandScope("history", argsMap)

        and: "execute Liquibase tag command"
        executeCommandScope("status", argsMap)

        then: "execute metadata checking sql, obtain result set, compare it to expected result set"
        try {
            def resultSet = ((JdbcConnection) connection).createStatement().executeQuery("SELECT * FROM DATABASECHANGELOG")
            def generatedResultSetArray = mapResultSetToJSONArray(resultSet)
            connection.commit()
            def expectedResultSetArray = new JSONObject(expectedResultSet).getJSONArray(testInput.change)
            assert compareJSONArrays(generatedResultSetArray, expectedResultSetArray, JSONCompareMode.LENIENT)
        } catch (Exception exception) {
            Scope.getCurrentScope().getUI().sendMessage("Error executing metadata checking sql! " + exception.printStackTrace())
            Assert.fail exception.message
        }

        and: "check for actual presence of created object"
        try {
            ((JdbcConnection) connection).createStatement().executeQuery(checkObjectIsPresentSql)
        } catch (SQLException sqlException) {
            // Assume test object was not created after 'update' command execution and test failed.
            Scope.getCurrentScope().getUI().sendMessage("Error executing test table checking sql! " +
                    sqlException.printStackTrace())
            Assert.fail sqlException.message
        } finally {
            connection.commit()
        }

        cleanup: "rollback changes if we ran changeSet"
        if (shouldRunChangeSet) {
            strategy.performRollback(argsMap)
        }

        and: "check for actual absence of the object removed after 'rollback' command execution"
        if (shouldRunChangeSet) {
            try {
                def resultSet = ((JdbcConnection) connection).createStatement().executeQuery(checkObjectIsPresentSql)
                if (resultSet.next()) {
                    Scope.getCurrentScope().getUI().sendMessage("Rollback was not successful! " +
                            "The object was not removed after 'rollback' command: " +
                            resultSet.getMetaData().getTableName(0))
                    Assert.fail()
                }
            } catch (SQLException sqlException) {
                // Assume test object does not exist and 'rollback' was successful. Ignore exception.
                Scope.getCurrentScope().getUI().sendMessage("Rollback was successful. Removed object was not found. " +
                        sqlException.message)
            } finally {
                connection.commit()
            }
        }

        where: "test input in next data table"
        testInput << buildTestInput()
    }

    def cleanupSpec() {
        strategy.cleanupDatabase(databases)
    }
}
