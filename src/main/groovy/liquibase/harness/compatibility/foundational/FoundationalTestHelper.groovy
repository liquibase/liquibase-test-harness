package liquibase.harness.compatibility.foundational

import groovy.transform.ToString
import groovy.transform.builder.Builder
import liquibase.Scope
import liquibase.database.Database
import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.harness.util.FileUtils
import liquibase.harness.util.TestUtils
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException

class FoundationalTestHelper {

    final static String baseChangelogPath = "liquibase/harness/compatibility/foundational/changelogs"
    final static List supportedChangeLogFormats = ['xml', 'sql', 'json', 'yml', 'yaml'].asImmutable()

    static List<TestInput> buildTestInput() {
        String commandLineInputFormat = System.getProperty("inputFormat")
        if (commandLineInputFormat) {
            if (!supportedChangeLogFormats.contains(commandLineInputFormat)) {
                throw new IllegalArgumentException(commandLineInputFormat + " inputFormat is not supported")
            }
            TestConfig.instance.inputFormat = commandLineInputFormat
        }
        Scope.getCurrentScope().getUI().sendMessage("Only " + TestConfig.instance.inputFormat
                + " input files are taken into account for this test run")

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
                        .xmlChange(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath, "xml").get(changeLogEntry.key))
                        .jsonChange(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath, "json").get(changeLogEntry.key))
                        .ymlChange(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath, "yml").get(changeLogEntry.key))
                        .sqlChange(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath, "sql").get(changeLogEntry.key))
                        .database(databaseUnderTest.database)
                        .build())
            }
        }
        return inputList
    }

    /**
     * Runs a checking query and returns the {@link ResultSet} together with the {@link Connection} that the
     * caller is responsible for closing. The second element is non-null only when this method opened a brand
     * new connection (the firebird case) - it is {@code null} for the shared-connection branch, so the caller
     * can simply close whatever it gets back without ever closing the shared database connection.
     * The freshly opened connection cannot be closed here because the returned ResultSet would then be invalid;
     * on failure it is closed before re-throwing so it is not leaked.
     */
    static Tuple2<ResultSet, Connection> executeQuery(String pathToSql, TestInput testInput) throws SQLException {
        Connection newConnection = null
        try {
            ResultSet resultSet
            if (TestUtils.shouldOpenNewConnection(testInput.database.getConnection(), "firebird")) {
                newConnection = DriverManager.getConnection(testInput.url, testInput.username, testInput.password)
                resultSet = newConnection.createStatement().executeQuery(pathToSql)
            } else {
                JdbcConnection connection = (JdbcConnection) testInput.database.connection
                resultSet = connection.createStatement().executeQuery(pathToSql)
                testInput.database.connection.autoCommit ?: testInput.database.connection.commit()
            }
            return new Tuple2<>(resultSet, newConnection)
        } catch (SQLException sqlException) {
            newConnection?.close()
            throw sqlException
        }
    }

    /**
     * Vendor-specific message fragments used to recognise an "object is absent" failure for the few drivers
     * that do not report the standard SQLState class "42" (Syntax Error or Access Rule Violation), e.g.
     * SQL Server (SQLState "S0002") and SQLite (null SQLState).
     */
    private final static List<String> OBJECT_NOT_FOUND_MESSAGES = [
            "does not exist",       // PostgreSQL, CockroachDB, EDB, Oracle (ORA-00942)
            "doesn't exist",        // MySQL, MariaDB, Percona, TiDB
            "no such table",        // SQLite
            "invalid object name",  // SQL Server
            "not found",            // HSQLDB, H2, DB2
            "unknown table",        // Informix
    ].asImmutable()

    /**
     * Distinguishes an expected "the rollback removed the object, so the checking query can no longer find it"
     * outcome from a genuine SQL failure (connection loss, permissions, malformed query, ...). Only the former
     * should be swallowed by the rollback absence-check; everything else must fail the test.
     */
    static boolean isObjectNotFoundException(SQLException sqlException) {
        String sqlState = sqlException.getSQLState()
        // SQLState class "42" covers "undefined table/object" for the vast majority of supported databases.
        if (sqlState?.startsWith("42")) {
            return true
        }
        String message = sqlException.getMessage()?.toLowerCase()
        return message != null && OBJECT_NOT_FOUND_MESSAGES.any { message.contains(it) }
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
        String xmlChange
        String jsonChange
        String ymlChange
        String sqlChange
        Database database
    }
}
