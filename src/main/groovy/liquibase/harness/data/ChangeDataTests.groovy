package liquibase.harness.data

import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.TestConfig
import org.json.JSONObject
import org.junit.Assert
import org.junit.Assume
import spock.lang.Specification
import spock.lang.Unroll
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.logging.Logger

import static liquibase.harness.util.JSONUtils.*
import static liquibase.harness.util.FileUtils.*
import static liquibase.harness.util.TestUtils.*
import static ChangeDataTestHelper.*

class ChangeDataTests extends Specification {

    @Unroll
    def "apply #testInput.changeData against #testInput.databaseName, #testInput.version; verify generated query, checking query and obtained result set"() {
        given: "read expected sql, checking sql and expected result set, create arguments map for executing command scope"
        String expectedSql = parseQuery(getExpectedSqlFileContent(testInput.changeData,
                testInput.databaseName, testInput.version, "liquibase/harness/data/expectedSql"))
        String checkingSql = parseQuery(getExpectedSqlFileContent(testInput.changeData,
                testInput.databaseName, testInput.version, "liquibase/harness/data/checkingSql"))
        JSONObject expectedResultSet =  new JSONObject(getExpectedJSONFileContent(testInput.changeData, testInput.databaseName,
                testInput.version, "liquibase/harness/data/expectedResultSet"))
        Map<String, Object> argsMap = new HashMap<>()
        argsMap.put("url", testInput.url)
        argsMap.put("username", testInput.username)
        argsMap.put("password", testInput.password)
        argsMap.put("changeLogFile", testInput.pathToChangeLogFile)
        argsMap.put("count", getChangeSetsCount(testInput.pathToChangeLogFile))

        and: "skip testcase if it's invalid for this combination of db type and/or version"
        Assume.assumeTrue(expectedSql, expectedSql == null || !expectedSql.toLowerCase().contains("invalid test"))

        and: "fail test if expectedResultSet is not provided"
        assert expectedResultSet != null: "No expectedResultSet for ${testInput.changeData} against " +
                "${testInput.database.shortName} ${testInput.database.databaseMajorVersion}." +
                "${testInput.database.databaseMinorVersion}"

        and: "fail test if checkingSql is not provided"
        assert checkingSql != null: "No checkingSql for ${testInput.changeData} against " +
                "${testInput.database.shortName} ${testInput.database.databaseMajorVersion}." +
                "${testInput.database.databaseMinorVersion}"

        when: "get sql that is generated for change set"
        def generatedSql = parseQuery(executeCommandScope("updateSql", argsMap).toString())

        then: "verify expected sql matches generated sql"
        if (expectedSql != null && !testInput.pathToChangeLogFile.endsWith(".sql")) {
            assert generatedSql == expectedSql: "Expected SQL does not match generated SQL. " +
                    "Deleting the existing expectedSql file will test that the new SQL works correctly " +
                    "and will auto-generate a new version if it passes"
            if (!TestConfig.instance.revalidateSql) {
                return //sql is right. Nothing more to test
            }
        }
        //TODO: Replace connection workaround for mysql and mariadb. Ticket DAT-8103
        Connection mysqlConnection
        if (testInput.databaseName == "mysql" || testInput.databaseName == "mariadb") {
            mysqlConnection = DriverManager.getConnection(testInput.url + "?" + "user=" + testInput.username + "&"
                    + "password=" + testInput.password)
            assert mysqlConnection.isValid(10)
        }
        def connection = testInput.database.getConnection()
        assert connection instanceof JdbcConnection: "We cannot verify the following SQL works " +
                "because the database is offline:\n${generatedSql}"

        when: "apply changeSet to DB,"
        executeCommandScope("update", argsMap)

        then: "obtain resultSet form the statement, compare expected resultSet to generated resultSet, apply rollback"
        try {
            def resultSet
            if (testInput.databaseName == "mysql" || testInput.databaseName == "mariadb") {
                resultSet = mysqlConnection.createStatement().executeQuery(checkingSql)
            } else {
                resultSet = connection.createStatement().executeQuery(checkingSql)
            }
            def generatedResultSet = new JSONObject()
            generatedResultSet.put(testInput.changeData, mapResultSetToJSONArray(resultSet))
            compareJSONObjects(generatedResultSet, expectedResultSet)
        } catch (Exception exception) {
            Logger.getLogger(this.class.name).severe("Error executing checking SQL! " + exception.printStackTrace())
            Assert.fail exception.message
        }
        executeCommandScope("rollbackCount", argsMap)

        and: "if expected sql is not provided save generated SQL as expected SQL"
        if (expectedSql == null && !testInput.pathToChangeLogFile.endsWith(".sql")) {
            saveAsExpectedSql(generatedSql, testInput)
        }
        if (testInput.databaseName == "mysql" || testInput.databaseName == "mariadb") {
            try {
                mysqlConnection.close()
            } catch (SQLException exception) {
                Logger.getLogger(this.class.name).severe("Failed to close jdbc connection! " + exception.printStackTrace())
            }
        }

        where: "test input in next data table"
        testInput << buildTestInput()
    }
}
