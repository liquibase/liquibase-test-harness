package liquibase.harness.util

import liquibase.command.CommandScope
import liquibase.exception.CommandExecutionException
import liquibase.harness.util.rollback.RollbackByTag
import liquibase.harness.util.rollback.RollbackStrategy
import liquibase.harness.util.rollback.RollbackToDate
import org.junit.Assert

import java.util.logging.Logger

import static liquibase.harness.util.FileUtils.deleteFile
import static liquibase.harness.util.FileUtils.deleteFile
import static liquibase.harness.util.FileUtils.deleteFile
import static liquibase.harness.util.FileUtils.deleteFile

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
            Assert.fail exception.message
        }
        return outputStream
    }

    static OutputStream executeCommandScope(String commandName, Map<String, Object> arguments, String dbName) {
        def commandScope = new CommandScope(commandName)
        def outputStream = new ByteArrayOutputStream()
        for (Map.Entry<String, Object> entry : arguments) {
            commandScope.addArgumentValue(entry.getKey(), entry.getValue())
        }
        commandScope.setOutput(outputStream)
        try {
            Logger.getLogger(this.class.name).info(String.format("Executing liquibase command: %s ", commandName))
            commandScope.execute()
            if (commandName.equals("generateChangelog") && dbName.equals("mssql")) {
                String testResourcesPath = System.getProperty("user.dir") + "/src/test/resources/liquibase/harness/compatibility/foundational/expectedChangeLog/mssql/objects/"
                deleteFile(testResourcesPath + "function/test_function.sql")
                deleteFile(testResourcesPath + "procedure/test_procedure.sql")
                deleteFile(testResourcesPath + "function/")
                deleteFile(testResourcesPath + "procedure/")
                deleteFile(testResourcesPath)
            }
        } catch (Exception exception) {
            if (exception instanceof CommandExecutionException && exception.toString().contains("is not available in SQL output mode")) {
                //Here we check whether updateSql command throws specific exception and skip it (updateSql doesn't work for SQLite for some change types)
                return outputStream
            }
            Logger.getLogger(this.class.name).severe("Failed to execute command scope for command " +
                    commandScope.getCommand().toString() + ". " + exception.printStackTrace())
            Logger.getLogger(this.class.name).info("If this is expected to be invalid query for this database/version, " +
                    "create an 'expectedSql.sql' file that starts with 'INVALID TEST' and an explanation of why.")
            Assert.fail exception.message
        }
        return outputStream
    }

    static RollbackStrategy chooseRollbackStrategy() {
        return "rollbackByTag".equalsIgnoreCase(System.getProperty("rollbackStrategy")) ? new RollbackByTag() : new RollbackToDate()
    }
}
