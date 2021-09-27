package liquibase.harness.data

import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.TestConfig
import org.json.JSONObject
import org.junit.Assert
import org.junit.Assume
import spock.lang.Specification
import spock.lang.Unroll
import java.sql.DriverManager
import java.sql.SQLException
import java.util.logging.Logger

import static liquibase.harness.util.JSONUtils.*
import static liquibase.harness.util.FileUtils.*
import static liquibase.harness.util.TestUtils.*
import static ChangeDataTestHelper.*

class ChangeDataTests extends Specification {

    @Unroll
    def "apply #testInput.changeData against #testInput.databaseName #testInput.version"() {
        given: "read expected sql, checking sql and expected result set, create arguments map for executing command scope"
        def expectedSql = parseQuery(getSqlFileContent(testInput.changeData, testInput.databaseName, testInput.version,
                "liquibase/harness/data/expectedSql"))
        def checkingSql = parseQuery(getSqlFileContent(testInput.changeData, testInput.databaseName, testInput.version,
                "liquibase/harness/data/checkingSql"))
        def expectedResultSet = getJSONFileContent(testInput.changeData, testInput.databaseName, testInput.version,
                "liquibase/harness/data/expectedResultSet")
        def argsMap = new HashMap<String, Object>()
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

        when: "get sql generated for the change set"
        def generatedSql = parseQuery(executeCommandScope("updateSql", argsMap).toString())

        then: "verify expected sql matches generated sql"
        if (expectedSql != null && !testInput.pathToChangeLogFile.endsWith(".sql")) {
            assert generatedSql == expectedSql: "Expected sql doesn't match generated sql. Deleting expectedSql file" +
                    " will test that new sql works correctly and will auto-generate a new version if it passes"
            if (!TestConfig.instance.revalidateSql) {
                return //sql is right. Nothing more to test
            }
        }
        //TODO: Replace connection workaround for mysql and mariadb. Ticket DAT-8103
        def mysqlConnection
        if (testInput.databaseName == "mysql" || testInput.databaseName == "mariadb") {
            mysqlConnection = DriverManager.getConnection(testInput.url + "?" + "user=" + testInput.username + "&"
                    + "password=" + testInput.password)
            assert mysqlConnection.isValid(10)
        }
        def connection = testInput.database.getConnection()
        assert connection instanceof JdbcConnection: "We cannot verify the following SQL works because the database " +
                "is offline:\n${generatedSql}"

        when: "apply changeSet to DB,"
        executeCommandScope("update", argsMap)

        then: "obtain resultSet form the statement, compare expected resultSet to generated resultSet"

        try {
            def resultSet
            if (testInput.databaseName == "mysql" || testInput.databaseName == "mariadb") {
                resultSet = mysqlConnection.createStatement().executeQuery(checkingSql)
            } else {
                resultSet = connection.createStatement().executeQuery(checkingSql)
            }
            def generatedResultSetArray = mapResultSetToJSONArray(resultSet)
            def expectedResultSetJSON = new JSONObject(expectedResultSet)
            def expectedResultSetArray = expectedResultSetJSON.getJSONArray(testInput.getChangeData())
            assert compareJSONArrays(generatedResultSetArray, expectedResultSetArray)
        } catch (Exception exception) {
            Logger.getLogger(this.class.name).severe("Error executing checking sql! " + exception.printStackTrace())
            Assert.fail exception.message
        }

        and: "if expected sql is not provided save generated sql as expected sql"
        if (expectedSql == null && !testInput.pathToChangeLogFile.endsWith(".sql")) {
            saveAsExpectedSql(generatedSql, testInput)
        }
        if (testInput.databaseName == "mysql" || testInput.databaseName == "mariadb") {
            try {
                mysqlConnection.close()
            } catch (SQLException exception) {
                Logger.getLogger(this.class.name).severe("Failed to close jdbc connection! " + exception.message)
            }
        }

        cleanup: "rollback changes"
        executeCommandScope("rollbackCount", argsMap)

        where: "test input in next data table"
        testInput << buildTestInput()
    }
}
