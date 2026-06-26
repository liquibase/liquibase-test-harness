package liquibase.harness.util

import liquibase.Scope
import liquibase.command.CommandScope
import liquibase.database.DatabaseConnection
import liquibase.exception.CommandExecutionException
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.rollback.RollbackByTag
import liquibase.harness.util.rollback.RollbackStrategy
import liquibase.harness.util.rollback.RollbackToDate
import liquibase.resource.SearchPathResourceAccessor
import org.junit.jupiter.api.Assertions

import java.util.logging.Level
import java.util.logging.Logger

/**
 * Test utilities for the Liquibase test harness.
 * 
 * Note: The parseQuery method filters out the Snowflake statement "alter session set jdbc_query_result_format = 'JSON'" 
 * which is automatically added by SnowflakeDatabase.configureSession() method when running on Java 17+.
 * This is necessary to make test assertions pass, as this statement is added to work around issues with 
 * the Snowflake driver's arrow support in Java 17+ but is not present in the expected SQL.
 */
class TestUtils {

    /**
     * Standardizes sql content. Parses 'clean' queries from database update sql script.
     * @param script
     * @return
     */
    static parseQuery(String script) {
        if (script) {
            script.replaceAll(/(?m)^--.*/, "") //remove comments
                    .replaceAll(/(?m)^CREATE TABLE .*\w*.*DATABASECHANGELOG.*/, "") //remove create table queries for databasechangelog* tables
                    .replaceAll(/(?m)^CREATE TABLE .*\w*.*databasechangelog.*/, "")
                    .replaceAll(/(?m)^INSERT INTO .*\w*.*DATABASECHANGELOG.*/, "") //remove insert queries for databasechangelog* tables
                    .replaceAll(/(?m)^INSERT INTO .*\w*.*databasechangelog.*/, "")
                    .replaceAll(/(?m)^UPDATE .*\w*.*DATABASECHANGELOG.*/, "") //remove update queries for databasechangelog* tables
                    .replaceAll(/(?m)^UPDATE .*\w*.*databasechangelog.*/, "")
                    .replaceAll(/(?m)^DELETE FROM .*\w*.*databasechangelog.*/, "") //remove delete queries for databasechangelog* tables
                    .replaceAll(/(?m)^DELETE FROM .*\w*.*DATABASECHANGELOG.*/, "")
                    .replaceAll(/(?m)^SET SEARCH_PATH.*/, "") //specific replacement for Postgres
                    .replaceAll(/\b(?:GO|USE lbcat)\b/, "") //specific replacement for MSSQL
                    .replaceAll(/(?m)^SET GLOBAL log_bin_trust_function_creators = 1/, "") //specific replacement for MySQL
                    .replaceAll(/(?i)alter\s+session\s+set\s+jdbc_query_result_format\s*=\s*['"]JSON['"]/, "") //remove Snowflake JDBC format setting
                    .replaceAll("(?m);\$", "") // remove semicolon
                    .replaceAll(/^(?:[\t ]*(?:\r?\n|\r))+/, "") //remove empty lines
                    .replaceAll(/(?m)^\s+/, "") //remove beginning whitespaces per line
                    .replaceAll(/(?m)\s+$/, "") //remove trailing whitespaces per line
                    .replaceAll("\r", "")
        }
    }

    static OutputStream executeCommandScope(String commandName, Map<String, Object> arguments) {
        return executeCommandScope(commandName, arguments, Collections.<String, Object> emptyMap())
    }

    static OutputStream executeCommandScope(String commandName, Map<String, Object> arguments, Map<String, Object> scopeValues) {
        def commandScope = new CommandScope(commandName)
        def outputStream = new ByteArrayOutputStream()
        for (Map.Entry<String, Object> entry : arguments) {
            commandScope.addArgumentValue(entry.getKey(), entry.getValue())
        }
        commandScope.setOutput(outputStream)
        runCommandScope(commandName, commandScope, scopeValues)
        return outputStream
    }

    static OutputStream executeCommandScopeWithSearchPathResourceAccessor(String commandName, Map<String, Object> arguments) {
        def resourceAccessor = new SearchPathResourceAccessor(".", Scope.getCurrentScope().getResourceAccessor())
        Map<String, Object> scopeValues = new HashMap<>()
        scopeValues.put(Scope.Attr.resourceAccessor.name(), resourceAccessor)
        return executeCommandScope(commandName, arguments, scopeValues)
    }

    /**
     * Executes the given command scope, optionally wrapped in a child scope, with shared error handling.
     * The "is not available in SQL output mode" exception is only swallowed for the updateSql command
     * (updateSql doesn't work for SQLite for some change types); any other failure fails the test.
     */
    private static void runCommandScope(String commandName, CommandScope commandScope, Map<String, Object> scopeValues) {
        try {
            Logger.getLogger(this.class.name).info(String.format("Executing liquibase command: %s ", commandName))
            if (scopeValues) {
                Scope.child(scopeValues, new Scope.ScopedRunner() {
                    @Override
                    void run() throws Exception {
                        commandScope.execute()
                    }
                })
            } else {
                commandScope.execute()
            }
        } catch (Exception exception) {
            if ("updateSql".equalsIgnoreCase(commandName) && exception instanceof CommandExecutionException
                    && exception.toString().contains("is not available in SQL output mode")) {
                return
            }
            Logger.getLogger(this.class.name).log(Level.SEVERE, "Failed to execute command scope for command " +
                    commandScope.getCommand().toString() + ".", exception)
            Logger.getLogger(this.class.name).info("If this is expected to be invalid query for this database/version, " +
                    "create an 'expectedSql.sql' file that starts with 'INVALID TEST' and an explanation of why.")
            Assertions.fail exception.message
        }
    }

    static RollbackStrategy chooseRollbackStrategy() {
        String requestedStrategy = System.getProperty("rollbackStrategy")
        if (requestedStrategy != null) {
            return "rollbackByTag".equalsIgnoreCase(requestedStrategy) ? new RollbackByTag() : new RollbackToDate()
        }
        // Informix records DATEEXECUTED on the DB server clock, which can drift against the harness JVM's
        // UTC timestamp by more than RollbackToDate's 1-second, second-truncated buffer. When that happens
        // a changeset's DATEEXECUTED lands at/before the rollback date and is never rolled back, leaking rows
        // (e.g. the insert test's id=100 row) into later tests. Tag-based rollback is position-based and
        // immune to clock skew, so default Informix to it. Other databases keep the date-based default.
        // Only switch when every database under test is Informix (e.g. the informix job runs with -DdbName=informix);
        // the diff/diffChangelog jobs run unfiltered and would otherwise tag every database in the config - including
        // ones their create-infra step never starts, causing connection-refused failures in setupSpec.
        List<DatabaseUnderTest> databasesUnderTest = TestConfig.getInstance().getFilteredDatabasesUnderTest()
        if (!databasesUnderTest.isEmpty() && databasesUnderTest.every { it.name?.toLowerCase()?.contains("informix") }) {
            return new RollbackByTag()
        }
        return new RollbackToDate()
    }

    /**
     * Returns true if a fresh JDBC connection should be opened for the given connection, either because it is
     * already closed or because its product name matches one of the supplied database names.
     */
    static boolean shouldOpenNewConnection(DatabaseConnection connection, String... dbNames) {
        return connection.isClosed() || Arrays.stream(dbNames).anyMatch({ dbName -> connection.getDatabaseProductName().toLowerCase().contains(dbName) })
    }
}
