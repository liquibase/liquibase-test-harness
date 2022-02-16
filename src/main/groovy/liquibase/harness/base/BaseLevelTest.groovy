package liquibase.harness.base

import liquibase.database.jvm.JdbcConnection
import org.json.JSONObject
import org.junit.Assert
import spock.lang.Specification

import java.sql.SQLException
import java.util.logging.Logger

import static liquibase.harness.util.FileUtils.*
import static liquibase.harness.util.JSONUtils.*
import static liquibase.harness.util.TestUtils.*

import static BaseLevelTestHelper.buildTestInput

class BaseLevelTest extends Specification {

    def "run base level test"() {
        given: "read input data"
        String checkingSql = getSqlFileContent(testInput.change, testInput.databaseName, testInput.version,
                "liquibase/harness/base/checkingSql")
        String expectedResultSet = getJSONFileContent(testInput.change, testInput.databaseName, testInput.version,
                "liquibase/harness/base/expectedResultSet")
        Map<String, Object> argsMap = new HashMap()
        argsMap.put("url", testInput.url)
        argsMap.put("username", testInput.username)
        argsMap.put("password", testInput.password)
        argsMap.put("changeLogFile", testInput.pathToChangeLogFile)
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

        then: "execute checking sql via JDBC connection, obtain result set and compare it to expected result set"
        try {
            def resultSet = ((JdbcConnection) connection).createStatement().executeQuery(checkingSql)
            def generatedResultSetArray = mapResultSetToJSONArray(resultSet)
            connection.commit()
            def expectedResultSetJSON = new JSONObject(expectedResultSet)
            def expectedResultSetArray = expectedResultSetJSON.getJSONArray(testInput.change)
            assert compareJSONArraysExtensible(generatedResultSetArray, expectedResultSetArray)
        } catch (Exception exception) {
            Logger.getLogger(this.class.name).severe("Error executing checking sql! " + exception.printStackTrace())
            Assert.fail exception.message
        }

        cleanup: "rollback changes if we ran changeSet"
        if (shouldRunChangeSet) {
            try {
                ((JdbcConnection) connection).createStatement().execute("DROP TABLE test_table;")
                ((JdbcConnection) connection).createStatement().execute("DELETE FROM DATABASECHANGELOG WHERE ID = '1';")
                connection.commit()
            } catch (SQLException exception) {
                Logger.getLogger(this.class.name).severe("Error executing cleanup sql! " + exception.printStackTrace())
                Assert.fail exception.message
            }
        }

        where: "test input in next data table"
        testInput << buildTestInput()
    }
}
