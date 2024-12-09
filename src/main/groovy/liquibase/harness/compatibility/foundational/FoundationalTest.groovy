package liquibase.harness.compatibility.foundational

import liquibase.Scope
import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.rollback.RollbackStrategy
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
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
import static FoundationalTestHelper.*

@Unroll
class FoundationalTest extends Specification {
    @Shared
    RollbackStrategy strategy
    @Shared
    List<DatabaseUnderTest> databases

    def setupSpec() {
        databases = TestConfig.instance.getFilteredDatabasesUnderTest()
        strategy = chooseRollbackStrategy()
        strategy.prepareForRollback(databases)
    }

    def "apply #testInput.change against #testInput.databaseName #testInput.version"() {
        given: "read input data"
        String basePath = "liquibase/harness/compatibility/foundational/"

        String expectedResultSet = getJSONFileContent(testInput.change, testInput.databaseName, testInput.version,
                "${basePath}/expectedResultSet")
        Map<String, Object> argsMap = new HashMap()
        argsMap.put("url", testInput.url)
        argsMap.put("username", testInput.username)
        argsMap.put("password", testInput.password)

        ArrayList<String> changelogList = new ArrayList<>()
        changelogList.add(testInput.xmlChange)
        changelogList.add(testInput.jsonChange)
        changelogList.add(testInput.ymlChange)
        changelogList.add(testInput.sqlChange)

        ArrayList<String> checkingSqlList = new ArrayList<>()
        checkingSqlList.add(getFileContent(testInput.change, testInput.databaseName, testInput.version, "/${basePath}checkingSql/${testInput.change}", "Xml.sql"))
        checkingSqlList.add(getFileContent(testInput.change, testInput.databaseName, testInput.version, "/${basePath}checkingSql/${testInput.change}","Yaml.sql"))
        checkingSqlList.add(getFileContent(testInput.change, testInput.databaseName, testInput.version, "/${basePath}checkingSql/${testInput.change}","Json.sql"))
        checkingSqlList.add(getFileContent(testInput.change, testInput.databaseName, testInput.version, "/${basePath}checkingSql/${testInput.change}","Sql.sql"))

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

        and: "execute Liquibase validate command to ensure a changelog is valid"
        for (int i = 0; i < changelogList.size(); i++) {
            argsMap.put("changeLogFile", changelogList.get(i))
            executeCommandScope("validate", argsMap)
        }
        //Doesn't work for sql-formatted changelogs. https://github.com/liquibase/liquibase/issues/1675 , https://github.com/liquibase/liquibase/issues/1118

        when: "execute XML, YAML, SQL and JSON formatted changelogs using liquibase update command"
        for (int i = 0; i < changelogList.size(); i++) {
            argsMap.put("changeLogFile", changelogList.get(i))
            executeCommandScope("update", argsMap)
        }

        and: "execute Liquibase tag command. Tagging last row of DATABASECHANGELOG table (SQL-formatted changelog)"
        argsMap.remove("changeLogFile")
        argsMap.put("tag", "test_tag")
        executeCommandScope("tag", argsMap)
        //Doesn't work properly for SQLite https://github.com/liquibase/liquibase/issues/3304

        and: "execute Liquibase history command"
        executeCommandScope("history", argsMap)

        and: "execute Liquibase status command"
        for (int i = 0; i < changelogList.size(); i++) {
            argsMap.put("changeLogFile", changelogList.get(i))
            assert executeCommandScope("status", argsMap).toString().contains("is up to date")
        }

        then: "execute metadata checking sql, obtain result set, compare it to expected result set"
        JSONArray generatedResultSetArray
        Connection newConnection
        try {
            ResultSet resultSet
            if (shouldOpenNewConnection(connection, "sqlite")) {
                newConnection = DriverManager.getConnection(testInput.url, testInput.username, testInput.password)
                resultSet = newConnection.createStatement().executeQuery("SELECT * FROM DATABASECHANGELOG")
            } else if (shouldOpenNewConnection(connection, "informix")) {
                resultSet = ((JdbcConnection) connection).createStatement().executeQuery("SELECT * FROM DATABASECHANGELOG")
            } else {
                resultSet = ((JdbcConnection) connection).createStatement().executeQuery("SELECT * FROM DATABASECHANGELOG")
                connection.autoCommit ?: connection.commit()
            }
            generatedResultSetArray = mapResultSetToJSONArray(resultSet)

            def expectedResultSetArray = new JSONObject(expectedResultSet).getJSONArray(testInput.change)
            assert compareJSONArrays(generatedResultSetArray, expectedResultSetArray, JSONCompareMode.LENIENT)
        } catch (Exception exception) {
            Scope.getCurrentScope().getUI().sendMessage("Error executing metadata checking sql! " + exception.printStackTrace())
            Assertions.fail exception.message
        } finally {
            newConnection == null ?: newConnection.close()

        }

        and: "check for actual presence of created object"
        for (int i = 0; i < checkingSqlList.size(); i++) {
            try {
                executeQuery(checkingSqlList.get(i), testInput)
            } catch (SQLException sqlException) {
                // Assume test object was not created after 'update' command execution and test failed.
                Scope.getCurrentScope().getUI().sendMessage("Error executing test table checking sql! " +
                        sqlException.printStackTrace())
                Assertions.fail sqlException.message
            }
        }

        cleanup: "rollback changes if we ran changeSet"
        if (shouldRunChangeSet) {
            for (int i = 0; i < changelogList.size(); i++) {//TODO rethink rollback logic to do it only once
                argsMap.put("changeLogFile", changelogList.get(i))
                strategy.performRollback(argsMap)
            }
        }

        and: "check for actual absence of the object removed after 'rollback' command execution"
        if (shouldRunChangeSet) {
            for (int i = 0; i < checkingSqlList.size(); i++) {
                try {
                    ResultSet resultSet = executeQuery(checkingSqlList.get(i), testInput)
                    if (resultSet.next()) {
                        Scope.getCurrentScope().getUI().sendMessage("ERROR!: Rollback was not successful! " +
                                "The object was not removed after 'rollback' command: " +
                                resultSet.getMetaData().getTableName(0))
                        Assertions.fail()
                    }
                } catch (ignored) {
                    (connection.isClosed() || connection.autoCommit) ?: connection.commit()
                    // Assume test object does not exist and 'rollback' was successful. Ignore exception.
                    Scope.getCurrentScope().getUI().sendMessage("Rollback was successful. Removed object was not found.")
                }
            }
        }

        where: "test input in next data table"
        testInput << buildTestInput()
    }

    def cleanupSpec() {
        strategy.cleanupDatabase(databases)
    }
}
