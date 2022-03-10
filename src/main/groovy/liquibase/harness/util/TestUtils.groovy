package liquibase.harness.util

import liquibase.command.CommandScope
import liquibase.exception.CommandExecutionException
import org.junit.Assert
import org.w3c.dom.NodeList
import org.xml.sax.SAXException
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import java.nio.charset.StandardCharsets
import java.util.logging.Logger
import java.util.stream.Collectors

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
                    .replaceAll("(?m);\$", "") // remove semicolon
                    .replaceAll(/^(?:[\t ]*(?:\r?\n|\r))+/, "") //remove empty lines
                    .replaceAll(/(?m)^\s+/, "") //remove beginning whitespaces per line
                    .replaceAll(/(?m)\s+$/, "") //remove trailing whitespaces per line
                    .replaceAll("\r", "")
        }
    }

    static OutputStream  executeCommandScope(String commandName, Map<String, Object> arguments) {
        def commandScope = new CommandScope(commandName)
        def outputStream = new ByteArrayOutputStream()
        for (Map.Entry<String, Object> entry : arguments) {
            commandScope.addArgumentValue(entry.getKey(), entry.getValue())
        }
        commandScope.setOutput(outputStream)
        try {
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

    static Integer getChangeSetsCount(String pathToChangeLogFile) {
        if (pathToChangeLogFile.endsWith("xml")) {
            return getChangeSetsCountXml(pathToChangeLogFile)
        } else if (pathToChangeLogFile.endsWith("sql")) {
            return getChangeSetsCountSql(pathToChangeLogFile)
        }
        //TODO: add methods for yml and json formatted changelogs
        return 0
    }

    static Integer getChangeSetsCountSql(String pathToChangeLogFile) {
        return FileUtils.getResourceContent("/" + pathToChangeLogFile).findAll("--changeset").size()
    }

    static Integer getChangeSetsCountXml(String pathToChangeLogFile) {
        try {
            def documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            def document = documentBuilder.parse(new FileInputStream("src/main/resources/" + pathToChangeLogFile))
            NodeList name = document.getElementsByTagName("changeSet")
            return name.getLength()
        } catch (ParserConfigurationException | SAXException | IOException exception) {
            Logger.getLogger(this.class.name).severe("Failed to read from changelog file while getting changesets count! " +
                    exception)
        }
        return 0
    }


}
