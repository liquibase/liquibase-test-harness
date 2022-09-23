package liquibase.harness.base

import liquibase.Scope
import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.rollback.RollbackStrategy
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert
import org.skyscreamer.jsonassert.JSONCompareMode
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException

import static liquibase.harness.util.FileUtils.*
import static liquibase.harness.util.JSONUtils.*
import static liquibase.harness.util.TestUtils.*
import static liquibase.harness.base.BaseCompatibilityTestHelper.*

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
        String expectedResultSet = getJSONFileContent(testInput.change, testInput.databaseName, testInput.version,
                "liquibase/harness/base/expectedResultSet")
        Map<String, Object> argsMap = new HashMap()
        argsMap.put("url", testInput.url)
        argsMap.put("username", testInput.username)
        argsMap.put("password", testInput.password)
        String basePath = "liquibase/harness/base/"
        String pathToXmlChangelogFile = "${basePath}changelogs/${testInput.change}.xml"
        String pathToYamlChangelogFile = "${basePath}changelogs/${testInput.change}.yml"
        String pathToJsonChangelogFile = "${basePath}changelogs/${testInput.change}.json"
        String pathToSqlChangelogFile = "${basePath}changelogs/${testInput.change}.sql"
        String pathToXmlCheckingSqlFile =
                getResourceContent("/${basePath}checkingSql/${testInput.change}/${testInput.change}Xml.sql")
        String pathToYamlCheckingSqlFile =
                getResourceContent("/${basePath}checkingSql/${testInput.change}/${testInput.change}Yaml.sql")
        String pathToJsonCheckingSqlFile =
                getResourceContent("/${basePath}checkingSql/${testInput.change}/${testInput.change}Json.sql")
        String pathToSqlCheckingSqlFile =
                getResourceContent("/${basePath}checkingSql/${testInput.change}/${testInput.change}Sql.sql")
        boolean shouldRunChangeSet

        and: "fail test if expectedResultSet is not provided"
        shouldRunChangeSet = expectedResultSet != null
        assert shouldRunChangeSet: "No expectedResultSet for ${testInput.change} against " +
                "${testInput.database.shortName} ${testInput.database.databaseMajorVersion}." +
                "${testInput.database.databaseMinorVersion}"

        and: "check database under test is online"
        def connection = testInput.database.getConnection()
        shouldRunChangeSet = connection instanceof JdbcConnection
        assert shouldRunChangeSet: "Database ${testInput.databaseName} ${testInput.version} is offline!"

        and: "execute Liquibase validate command to ensure a chagelog is valid"
        argsMap.put("changeLogFile", pathToXmlChangelogFile)
        executeCommandScope("validate", argsMap)
        argsMap.put("changeLogFile", pathToYamlChangelogFile)
        executeCommandScope("validate", argsMap)
        argsMap.put("changeLogFile", pathToJsonChangelogFile)
        executeCommandScope("validate", argsMap)
        argsMap.put("changeLogFile", pathToSqlChangelogFile)
        executeCommandScope("validate", argsMap) //Doesn't work for sql-formatted changelogs :(

        when: "execute XML, YAML, SQL and JSON formatted changelogs using liquibase update command"
        argsMap.put("changeLogFile", pathToXmlChangelogFile)
        executeCommandScope("update", argsMap)
        argsMap.put("changeLogFile", pathToYamlChangelogFile)
        executeCommandScope("update", argsMap)
        argsMap.put("changeLogFile", pathToJsonChangelogFile)
        executeCommandScope("update", argsMap)
        argsMap.put("changeLogFile", pathToSqlChangelogFile)
        executeCommandScope("update", argsMap)

        and: "execute Liquibase tag command"
        argsMap.remove("changeLogFile")
        argsMap.put("tag", "test_tag")
        executeCommandScope("tag", argsMap)

        and: "execute Liquibase history command"
        executeCommandScope("history", argsMap)

        and: "execute Liquibase status command"
        argsMap.put("changeLogFile", pathToXmlChangelogFile)
        executeCommandScope("status", argsMap)
        argsMap.put("changeLogFile", pathToYamlChangelogFile)
        executeCommandScope("status", argsMap)
        argsMap.put("changeLogFile", pathToJsonChangelogFile)
        executeCommandScope("status", argsMap)
        argsMap.put("changeLogFile", pathToSqlChangelogFile)
        executeCommandScope("status", argsMap)

        then: "execute metadata checking sql, obtain result set, compare it to expected result set"
        Connection newConnection
        JSONArray generatedResultSetArray
        try {
            ResultSet resultSet
            if (connection.isClosed()) {
                newConnection = DriverManager.getConnection(testInput.url, testInput.username, testInput.password)
                resultSet = newConnection.createStatement().executeQuery("SELECT * FROM DATABASECHANGELOG")
                generatedResultSetArray = mapResultSetToJSONArray(resultSet)
                if (!newConnection.autoCommit) {
                    newConnection.commit()
                }
            } else {
                resultSet = ((JdbcConnection) connection).createStatement().executeQuery("SELECT * FROM DATABASECHANGELOG")
                generatedResultSetArray = mapResultSetToJSONArray(resultSet)
                if (!connection.autoCommit) {
                    connection.commit()
                }
            }
            def expectedResultSetArray = new JSONObject(expectedResultSet).getJSONArray(testInput.change)
            assert compareJSONArrays(generatedResultSetArray, expectedResultSetArray, JSONCompareMode.LENIENT)
        } catch (Exception exception) {
            Scope.getCurrentScope().getUI().sendMessage("Error executing metadata checking sql! " + exception.printStackTrace())
            Assert.fail exception.message
        } finally {
            if (newConnection != null) {
                newConnection.close()
            }
        }

        and: "check for actual presence of created object"
        try {
            executeQuery(pathToXmlCheckingSqlFile, testInput)
            executeQuery(pathToYamlCheckingSqlFile, testInput)
            executeQuery(pathToJsonCheckingSqlFile, testInput)
            executeQuery(pathToSqlCheckingSqlFile, testInput)
        } catch (SQLException sqlException) {
            // Assume test object was not created after 'update' command execution and test failed.
            Scope.getCurrentScope().getUI().sendMessage("Error executing test table checking sql! " +
                    sqlException.printStackTrace())
            Assert.fail sqlException.message
        }

        cleanup: "rollback changes if we ran changeSet"
        if (shouldRunChangeSet) {
            argsMap.put("changeLogFile", pathToXmlChangelogFile)
            strategy.performRollback(argsMap)
            argsMap.put("changeLogFile", pathToYamlChangelogFile)
            strategy.performRollback(argsMap)
            argsMap.put("changeLogFile", pathToJsonChangelogFile)
            strategy.performRollback(argsMap)
            argsMap.put("changeLogFile", pathToSqlChangelogFile)
            strategy.performRollback(argsMap)
        }

        and: "check for actual absence of the object removed after 'rollback' command execution"
        if (shouldRunChangeSet) {
            try {
                ResultSet resultSetXml = executeQuery(pathToXmlCheckingSqlFile, testInput)
                if (resultSetXml.next()) {
                    Scope.getCurrentScope().getUI().sendMessage("Rollback was not successful! " +
                            "The object was not removed after 'rollback' command: " +
                            resultSetXml.getMetaData().getTableName(0))
                    Assert.fail()
                }
            } catch (ignored) {
                // Assume test object does not exist and 'rollback' was successful. Ignore exception.
                Scope.getCurrentScope().getUI().sendMessage("Rollback was successful. Removed object was not found.")
            }
            try {
                ResultSet resultSetYaml = executeQuery(pathToYamlCheckingSqlFile, testInput)
                if (resultSetYaml.next()) {
                    Scope.getCurrentScope().getUI().sendMessage("Rollback was not successful! " +
                            "The object was not removed after 'rollback' command: " +
                            resultSetYaml.getMetaData().getTableName(0))
                    Assert.fail()
                }
            } catch (ignored) {
                // Assume test object does not exist and 'rollback' was successful. Ignore exception.
                Scope.getCurrentScope().getUI().sendMessage("Rollback was successful. Removed object was not found.")
            }
            try {
                ResultSet resultSetJson = executeQuery(pathToJsonCheckingSqlFile, testInput)
                if (resultSetJson.next()) {
                    Scope.getCurrentScope().getUI().sendMessage("Rollback was not successful! " +
                            "The object was not removed after 'rollback' command: " +
                            resultSetJson.getMetaData().getTableName(0))
                    Assert.fail()
                }
            } catch (ignored) {
                // Assume test object does not exist and 'rollback' was successful. Ignore exception.
                Scope.getCurrentScope().getUI().sendMessage("Rollback was successful. Removed object was not found.")
            }
            try {
                ResultSet resultSetSql = executeQuery(pathToSqlCheckingSqlFile, testInput)
                if (resultSetSql.next()) {
                    Scope.getCurrentScope().getUI().sendMessage("Rollback was not successful! " +
                            "The object was not removed after 'rollback' command: " +
                            resultSetSql.getMetaData().getTableName(0))
                    Assert.fail()
                }
            } catch (ignored) {
                // Assume test object does not exist and 'rollback' was successful. Ignore exception.
                Scope.getCurrentScope().getUI().sendMessage("Rollback was successful. Removed object was not found.")
            }
        }

        where: "test input in next data table"
        testInput << buildTestInput()
    }

    def cleanupSpec() {
        strategy.cleanupDatabase(databases)
    }
}
