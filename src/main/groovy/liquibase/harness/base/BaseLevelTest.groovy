package liquibase.harness.base

import liquibase.Scope
import liquibase.database.jvm.JdbcConnection
import org.json.JSONObject
import org.junit.Assert
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.SQLException
import java.text.SimpleDateFormat

import static liquibase.harness.util.FileUtils.*
import static liquibase.harness.util.JSONUtils.*
import static liquibase.harness.util.TestUtils.*
import static BaseLevelTestHelper.buildTestInput

@Unroll
class BaseLevelTest extends Specification {

    def "run base level test #testInput.change against #testInput.databaseName #testInput.version"() {
        given: "read input data"
        String checkingSql = getSqlFileContent(testInput.change, testInput.databaseName, testInput.version,
                "liquibase/harness/base/checkingSql")
        String expectedResultSet = getJSONFileContent(testInput.change, testInput.databaseName, testInput.version,
                "liquibase/harness/base/expectedResultSet")
        String testTableCheckSql = "SELECT * FROM test_table"
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss")
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
        Map<String, Object> argsMap = new HashMap()
        argsMap.put("url", testInput.url)
        argsMap.put("username", testInput.username)
        argsMap.put("password", testInput.password)
        argsMap.put("changeLogFile", testInput.pathToChangeLogFile)
        argsMap.put("date", sdf.format(new Date(System.currentTimeMillis() - 2000)))
        boolean shouldRunChangeSet

        and: "fail test if checkingSql is not provided"
        shouldRunChangeSet = checkingSql != null
        assert shouldRunChangeSet: "No checkingSql for ${testInput.change} against " +
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

        when: "execute SQL formatted changelog using liquibase update command"
        executeCommandScope("update", argsMap)

        then: "execute checking sql, obtain result set, compare it to expected result set"
        try {
            def resultSet = ((JdbcConnection) connection).createStatement().executeQuery(checkingSql)
            def generatedResultSetArray = mapResultSetToJSONArray(resultSet)
            connection.commit()
            def expectedResultSetJSON = new JSONObject(expectedResultSet)
            def expectedResultSetArray = expectedResultSetJSON.getJSONArray(testInput.change)
            assert compareJSONArraysExtensible(generatedResultSetArray, expectedResultSetArray)
        } catch (Exception exception) {
            Scope.getCurrentScope().getUI().sendMessage("Error executing checking sql! " + exception.printStackTrace())
            Assert.fail exception.message
        }

        and: "check for actual presence of created object"
        try {
            ((JdbcConnection) connection).createStatement().executeQuery(testTableCheckSql)
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
            executeCommandScope("rollbackToDate", argsMap)
        }

        and: "check for actual absence of the object removed after 'rollback' command execution"
        if (shouldRunChangeSet) {
            try {
                def resultSet = ((JdbcConnection) connection).createStatement().executeQuery(testTableCheckSql)
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
}
