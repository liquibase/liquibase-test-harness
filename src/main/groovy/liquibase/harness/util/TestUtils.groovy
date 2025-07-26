package liquibase.harness.util

import liquibase.Scope
import liquibase.command.CommandScope
import liquibase.exception.CommandExecutionException
import liquibase.harness.util.rollback.RollbackByTag
import liquibase.harness.util.rollback.RollbackStrategy
import liquibase.harness.util.rollback.RollbackToDate
import liquibase.resource.SearchPathResourceAccessor
import org.junit.jupiter.api.Assertions

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
                    .replaceAll(/(?m)^CREATE TABLE (?!DATABASECHANGELOG\s|DATABASECHANGELOGLOCK\s).*DATABASECHANGELOG.*/, "") //remove create table queries for custom databasechangelog* tables
                    .replaceAll(/(?m)^CREATE TABLE (?!databasechangelog\s|databasechangeloglock\s).*databasechangelog.*/, "")
                    .replaceAll(/(?m)^INSERT INTO (?!DATABASECHANGELOG\s|DATABASECHANGELOGLOCK\s).*DATABASECHANGELOG.*/, "") //remove insert queries for custom databasechangelog* tables
                    .replaceAll(/(?m)^INSERT INTO (?!databasechangelog\s|databasechangeloglock\s).*databasechangelog.*/, "")
                    .replaceAll(/(?m)^UPDATE (?!DATABASECHANGELOG\s|DATABASECHANGELOGLOCK\s).*DATABASECHANGELOG.*/, "") //remove update queries for custom databasechangelog* tables
                    .replaceAll(/(?m)^UPDATE (?!databasechangelog\s|databasechangeloglock\s).*databasechangelog.*/, "")
                    .replaceAll(/(?m)^DELETE FROM (?!databasechangelog\s|DATABASECHANGELOGLOCK\s).*databasechangelog.*/, "") //remove delete queries for custom databasechangelog* tables
                    .replaceAll(/(?m)^DELETE FROM (?!DATABASECHANGELOG\s|databasechangeloglock\s).*DATABASECHANGELOG.*/, "")
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
        def commandScope = new CommandScope(commandName)
        def outputStream = new ByteArrayOutputStream()
        for (Map.Entry<String, Object> entry : arguments) {
            commandScope.addArgumentValue(entry.getKey(), entry.getValue())
        }
        commandScope.setOutput(outputStream)
        try {
            Logger.getLogger(this.class.name).info(String.format("Executing liquibase command: %s ", commandName))
            commandScope.execute()
        } catch (Exception exception) {
            if (exception instanceof CommandExecutionException && exception.toString().contains("is not available in SQL output mode")) {
                //Here we check whether updateSql command throws specific exception and skip it (updateSql doesn't work for SQLite for some change types)
                return outputStream
            }
            Logger.getLogger(this.class.name).severe("Failed to execute command scope for command " +
                    commandScope.getCommand().toString() + ". " + exception.printStackTrace())
            Logger.getLogger(this.class.name).info("If this is expected to be invalid query for this database/version, " +
                    "create an 'expectedSql.sql' file that starts with 'INVALID TEST' and an explanation of why.")
            Assertions.fail exception.message
        }
        return outputStream
    }

    static OutputStream executeCommandScope(String commandName, Map<String, Object> arguments, Map<String,Object> scopeValues) {
        def commandScope = new CommandScope(commandName)
        def outputStream = new ByteArrayOutputStream()
        for (Map.Entry<String, Object> entry : arguments) {
            commandScope.addArgumentValue(entry.getKey(), entry.getValue())
        }
        commandScope.setOutput(outputStream)
        try {
            Logger.getLogger(this.class.name).info(String.format("Executing liquibase command: %s ", commandName))
            Scope.child(scopeValues, new Scope.ScopedRunner() {
                @Override
                void run() throws Exception {
                    commandScope.execute()
                }
            })

        } catch (Exception exception) {
            if (exception instanceof CommandExecutionException && exception.toString().contains("is not available in SQL output mode")) {
                //Here we check whether updateSql command throws specific exception and skip it (updateSql doesn't work for SQLite for some change types)
                return outputStream
            }
            Logger.getLogger(this.class.name).severe("Failed to execute command scope for command " +
                    commandScope.getCommand().toString() + ". " + exception.printStackTrace())
            Logger.getLogger(this.class.name).info("If this is expected to be invalid query for this database/version, " +
                    "create an 'expectedSql.sql' file that starts with 'INVALID TEST' and an explanation of why.")
            Assertions.fail exception.message
        }
        return outputStream
    }


    static OutputStream executeCommandScopeWithSearchPathResourceAccessor(String commandName, Map<String, Object> arguments) {
        def commandScope = new CommandScope(commandName)
        def outputStream = new ByteArrayOutputStream()
        def resourceAccessor = new SearchPathResourceAccessor(".", Scope.getCurrentScope().getResourceAccessor())
        Map<String, Object> map = new HashMap<>();
        map.put(Scope.Attr.resourceAccessor.name(),resourceAccessor)

        for (Map.Entry<String, Object> entry : arguments) {
            commandScope.addArgumentValue(entry.getKey(), entry.getValue())
        }
        commandScope.setOutput(outputStream)
        try {
            Logger.getLogger(this.class.name).info(String.format("Executing liquibase command: %s ", commandName))
            Scope.child(map, new Scope.ScopedRunner() {
                @Override
                void run() throws Exception {
                    commandScope.execute()
                }
            })
        } catch (Exception exception) {
            if (exception instanceof CommandExecutionException && exception.toString().contains("is not available in SQL output mode")) {
                //Here we check whether updateSql command throws specific exception and skip it (updateSql doesn't work for SQLite for some change types)
                return outputStream
            }
            Logger.getLogger(this.class.name).severe("Failed to execute command scope for command " +
                    commandScope.getCommand().toString() + ". " + exception.printStackTrace())
            Logger.getLogger(this.class.name).info("If this is expected to be invalid query for this database/version, " +
                    "create an 'expectedSql.sql' file that starts with 'INVALID TEST' and an explanation of why.")
            Assertions.fail exception.message
        }
        return outputStream
    }

    static RollbackStrategy chooseRollbackStrategy() {
        return "rollbackByTag".equalsIgnoreCase(System.getProperty("rollbackStrategy")) ? new RollbackByTag() : new RollbackToDate()
    }
}
