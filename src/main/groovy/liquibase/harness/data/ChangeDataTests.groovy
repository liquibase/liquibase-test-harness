package liquibase.harness.data

/**
 * Tests for Liquibase change data.
 * 
 * Note: For Snowflake connections on Java 17+, the JDBC query result format is configured in 
 * SnowflakeDatabase.configureSession() to use JSON format instead of Arrow format
 * to work around issues with the Snowflake driver on Java 17+.
 */

import liquibase.Scope
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.rollback.RollbackStrategy
import liquibase.resource.ClassLoaderResourceAccessor
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assumptions
import org.skyscreamer.jsonassert.JSONCompareMode
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

import static liquibase.harness.data.ChangeDataTestHelper.buildTestInput
import static liquibase.harness.data.ChangeDataTestHelper.shouldOpenNewConnection
import static liquibase.harness.data.ChangeDataTestHelper.saveAsExpectedSql
import static liquibase.harness.util.FileUtils.getJSONFileContent
import static liquibase.harness.util.FileUtils.getSqlFileContent
import static liquibase.harness.util.JSONUtils.compareJSONArrays
import static liquibase.harness.util.JSONUtils.mapResultSetToJSONArray
import static liquibase.harness.util.TestUtils.*

class ChangeDataTests extends Specification {
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
    def "apply #testInput.changeData against #testInput.databaseName #testInput.version"() {
        given: "read expected sql, checking sql and expected result set, create arguments map for executing command scope"
        String expectedSql = parseQuery(getSqlFileContent(testInput.changeData, testInput.databaseName, testInput.version,
                "liquibase/harness/data/expectedSql"))
        String checkingSql = parseQuery(getSqlFileContent(testInput.changeData, testInput.databaseName, testInput.version,
                "liquibase/harness/data/checkingSql"))
        String expectedResultSet = getJSONFileContent(testInput.changeData, testInput.databaseName, testInput.version,
                "liquibase/harness/data/expectedResultSet")
        boolean shouldRunChangeSet
        Map<String, Object> argsMap = new HashMap()
        argsMap.put("url", testInput.url)
        argsMap.put("username", testInput.username)
        argsMap.put("password", testInput.password)
        argsMap.put("changeLogFile", testInput.pathToChangeLogFile)

        and: "ignore testcase if it's invalid for this combination of db type and/or version"
        shouldRunChangeSet = !expectedSql?.toLowerCase()?.contains("invalid test")
        Assumptions.assumeTrue(shouldRunChangeSet, expectedSql)

        and: "fail test if expectedResultSet is not provided"
        shouldRunChangeSet = expectedResultSet != null
        assert shouldRunChangeSet: "No expectedResultSet for ${testInput.changeData}!"

        and: "fail test if checkingSql is not provided"
        shouldRunChangeSet = checkingSql != null
        assert shouldRunChangeSet: "No checkingSql for ${testInput.changeData}!"

        and: "check database under test is online"
        shouldRunChangeSet = testInput.database.getConnection() instanceof JdbcConnection
        assert shouldRunChangeSet: "Database ${testInput.databaseName} ${testInput.version} is offline!"
        JdbcConnection connection = testInput.database.getConnection() as JdbcConnection

        when: "get sql generated for the change set"
        def generatedSql = parseQuery(executeCommandScope("updateSql", argsMap).toString())

        then: "verify expected sql matches generated sql"
        if (expectedSql != null && !testInput.pathToChangeLogFile.endsWith(".sql")) {
            shouldRunChangeSet = generatedSql == expectedSql
            if (!shouldRunChangeSet) {
                Scope.getCurrentScope().getUI().sendMessage("FAIL! Expected sql doesn't " +
                        "match generated sql! Deleting expectedSql file will test that new sql works correctly and " +
                        "will auto-generate a new version if it passes. \nEXPECTED SQL: \n" + expectedSql + " \n" +
                        "GENERATED SQL: \n" + generatedSql)
                assert generatedSql == expectedSql
            }
            if (!TestConfig.instance.revalidateSql) {
                return //sql is right. Nothing more to test
            }
        }

        when: "apply changeSet to DB,"
        executeCommandScope("update", argsMap)

        then: "obtain resultSet form the statement, compare expected resultSet to generated resultSet"
        Connection newConnection
        ResultSet resultSet
        JSONArray generatedResultSetArray
        try {
            //For embedded databases, let's create separate connection to run checking SQL
            if (shouldOpenNewConnection(connection, "sqlite", "snowflake", "postgres", "oracle", "mysql")) {
                newConnection = DriverManager.getConnection(testInput.url, testInput.username, testInput.password)
                
                resultSet = newConnection.createStatement().executeQuery(checkingSql)
            } else {
                connection.close()
                connection = DatabaseFactory.getInstance().openConnection(testInput.url, testInput.username, testInput.password,
                        null, new ClassLoaderResourceAccessor()) as JdbcConnection
                
                resultSet = connection.createStatement().executeQuery(checkingSql)
                connection.autoCommit ?: connection.commit()
            }
            generatedResultSetArray = mapResultSetToJSONArray(resultSet)

            def expectedResultSetJSON = new JSONObject(expectedResultSet)
            def expectedResultSetArray = expectedResultSetJSON.getJSONArray(testInput.getChangeData())
            assert compareJSONArrays(generatedResultSetArray, expectedResultSetArray, JSONCompareMode.NON_EXTENSIBLE)
        } catch (Exception exception) {
            Scope.getCurrentScope().getUI().sendMessage("Error executing checking sql! " + exception.printStackTrace())
            Assert.fail exception.message
        } finally {
            newConnection == null ?: newConnection.close()
        }

        and: "if expected sql is not provided save generated sql as expected sql"
        if (expectedSql == null && !testInput.pathToChangeLogFile.endsWith(".sql") && !generatedSql.isEmpty()) {
            saveAsExpectedSql(generatedSql, testInput)
        }

        cleanup: "rollback changes"
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
