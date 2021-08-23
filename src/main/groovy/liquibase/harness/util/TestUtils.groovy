package liquibase.harness.util

import liquibase.Liquibase
import liquibase.command.CommandScope
import liquibase.database.Database
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import org.junit.Assert

import java.util.logging.Logger

class TestUtils {

    static SortedMap<String, String> resolveInputFilePaths(DatabaseUnderTest database, String basePath, String inputFormat) {
        inputFormat = inputFormat ?: ""
        def returnPaths = new TreeMap<String, String>()
        for (String filePath : TestConfig.instance.resourceAccessor.list(null, basePath, true, true, false)) {
            def validFile = false
            //is it a common changelog?
            if (filePath =~ basePath+"/[\\w.]*\\."+inputFormat+"\$") {
                validFile = true
            } else if (filePath =~ basePath+"/${database.name}/[\\w.]*\\.${inputFormat}\$") {
                //is it a database-specific changelog?
                validFile = true
            } else if (filePath =~ basePath+"/${database.name}/${database.version}/[\\w.]*\\.${inputFormat}\$") {
                //is it a database-major-version specific changelog?
                validFile = true
            }
            if (validFile) {
                def fileName = filePath.replaceFirst(".*/", "").replaceFirst("\\.[^.]+\$", "")
                if (!returnPaths.containsKey(fileName) || returnPaths.get(fileName).length() < filePath.length()) {
                    returnPaths.put(fileName, filePath)
                }
            }
        }

        Logger.getLogger(this.class.name).info("Found " + returnPaths.size() + " changeLogs for " + database.name +
                "/" + database.version + " in "+basePath)
        return returnPaths
    }

    /**
     * Standardizes sql content. Parses 'clean' queries from database update sql script.
     * @param script
     * @return
     */
    static parseQuery(String script) {
        if (script) {
            script.replaceAll(/(?m)^--.*/, "") //remove comments
                    .replaceAll(/(?m)^CREATE TABLE .*\w*.DATABASECHANGELOG.*/, "") //remove create table queries for databasechangelog* tables
                    .replaceAll(/(?m)^CREATE TABLE .*\w*.databasechangelog.*/, "")
                    .replaceAll(/(?m)^INSERT INTO .*\w*.DATABASECHANGELOG.*/, "") //remove insert queries for databasechangelog* tables
                    .replaceAll(/(?m)^INSERT INTO .*\w*.databasechangelog.*/, "")
                    .replaceAll(/(?m)^UPDATE .*\w*.DATABASECHANGELOG.*/, "") //remove update queries for databasechangelog* tables
                    .replaceAll(/(?m)^UPDATE .*\w*.databasechangelog.*/, "")
                    .replaceAll(/(?m)^SET SEARCH_PATH.*/, "") //remove setting schema name for posqtgresql
                    .replaceAll(/(?m)^\s+/, "") //remove beginning whitespaces per line
                    .replaceAll(/(?m)\s+$/, "") //remove trailing whitespaces per line
                    .replaceAll(";", "")
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
            commandScope.execute()
        } catch (Throwable throwable) {
            println("Failed to execute command scope for command " + commandScope.getCommand().toString() + ". " + throwable)
            println("If this is expected to be invalid query for this database/version, create an 'expectedSql.sql' " +
                    "file that starts with 'INVALID TEST' and an explanation of why.")
            throwable.printStackTrace()
            Assert.fail throwable.message
        }
        return outputStream
    }

    //TODO: Think of removing usage of Liquibase object
    static Integer getChangeSetsCount(String pathToChangeLogFile, Database database) {
        Liquibase liquibase = new Liquibase(pathToChangeLogFile, TestConfig.instance.resourceAccessor, database)
        return liquibase.databaseChangeLog.changeSets.size()
    }
}
