package liquibase.harness.base

import groovy.transform.ToString
import groovy.transform.builder.Builder
import liquibase.database.Database
import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.harness.util.FileUtils
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException

class BaseCompatibilityTestHelper {

    final static String baseChangelogPath = "liquibase/harness/base/changelogs"

    static List<TestInput> buildTestInput() {

        List<TestInput> inputList = new ArrayList<>()
        DatabaseConnectionUtil databaseConnectionUtil = new DatabaseConnectionUtil()

        for (DatabaseUnderTest databaseUnderTest : databaseConnectionUtil
                .initializeDatabasesConnection(TestConfig.instance.getFilteredDatabasesUnderTest())) {
            for (def changeLogEntry : FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath, "xml").entrySet()) {
                inputList.add(TestInput.builder()
                        .databaseName(databaseUnderTest.name)
                        .url(databaseUnderTest.url)
                        .dbSchema(databaseUnderTest.dbSchema)
                        .username(databaseUnderTest.username)
                        .password(databaseUnderTest.password)
                        .version(databaseUnderTest.version)
                        .change(changeLogEntry.key)
                        .database(databaseUnderTest.database)
                        .build())
            }
        }
        return inputList
    }

    static ResultSet executeQuery(String pathToSql, TestInput testInput) throws SQLException {
        Connection newConnection
        ResultSet resultSet
        try {
            if (testInput.database.connection.isClosed()) {
                newConnection = DriverManager.getConnection(testInput.url, testInput.username, testInput.password)
                resultSet = newConnection.createStatement().executeQuery(pathToSql)
                if (!newConnection.autoCommit) {
                    newConnection.commit()
                }
                return resultSet
            } else {
                JdbcConnection connection = (JdbcConnection) testInput.database.connection
                resultSet = connection.createStatement().executeQuery(pathToSql)
                if (!testInput.database.connection.autoCommit) {
                    testInput.database.connection.commit()
                }
                return resultSet
            }
        } finally {
            if (newConnection != null) {
                newConnection.close()
            }
        }
    }

    @Builder
    @ToString(includeNames = true, includeFields = true, includePackage = false, excludes = 'database,password')
    static class TestInput {
        String databaseName
        String version
        String username
        String password
        String url
        String dbSchema
        String change
        Database database
    }
}
