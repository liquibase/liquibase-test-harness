package liquibase.harness.generateChangelog

import groovy.transform.ToString
import groovy.transform.builder.Builder
import liquibase.Scope
import liquibase.database.Database
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.harness.util.FileUtils
import liquibase.ui.UIService

class GenerateChangelogTestHelper {
    final static String baseChangelogPath = "liquibase/harness/generateChangelog/"
    final static UIService uiService = Scope.getCurrentScope().getUI()

    static List<TestInput> buildTestInput() {
        String commandLineChanges = System.getProperty("change")
        List commandLineChangesList = Collections.emptyList()
        if (commandLineChanges) {
            commandLineChangesList = Arrays.asList(commandLineChanges.contains(",")
                    ? commandLineChanges.split(",")
                    : commandLineChanges)
        }

        List<TestInput> inputList = new ArrayList<>()
        DatabaseConnectionUtil databaseConnectionUtil = new DatabaseConnectionUtil()
        for (DatabaseUnderTest databaseUnderTest : databaseConnectionUtil
                .initializeDatabasesConnection(TestConfig.instance.getFilteredDatabasesUnderTest())) {
            for (def changeLogEntry : FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                    "expectedChangeLog", "xml").entrySet()) {
                if (!commandLineChangesList || commandLineChangesList.contains(changeLogEntry.key)) {
                    inputList.add(TestInput.builder()
                            .databaseName(databaseUnderTest.name)
                            .url(databaseUnderTest.url)
                            .dbSchema(databaseUnderTest.dbSchema)
                            .username(databaseUnderTest.username)
                            .password(databaseUnderTest.password)
                            .version(databaseUnderTest.version)
                            .setupChangelogPath(changeLogEntry.value)
                            .insertChangelogPath(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                                    "stress/insert", "xml").get(changeLogEntry.key))
                            .updateChangelogPath(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                                    "stress/update", "xml").get(changeLogEntry.key))
                            .selectChangelogPath(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                                    "stress/select", "xml").get(changeLogEntry.key))
                            .xmlChangelogPath(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                                    "expectedChangeLog", "xml").get(changeLogEntry.key))
                            .jsonChangelogPath(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                                    "expectedChangeLog", "json").get(changeLogEntry.key))
                            .ymlChangelogPath(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                                    "expectedChangeLog", "yml").get(changeLogEntry.key))
                            .sqlChangelogPath(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                                    "expectedSql", "sql").get(changeLogEntry.key))
                            .change(changeLogEntry.key)
                            .database(databaseUnderTest.database)
                            .build())
                }
            }
        }
        return inputList
    }

    static validateSqlChangelog(String expectedSqlChangelog, String generatedSqlChangelog) {
        String replacementRegexp = "--(.*?)\r?\n" //removes all sql comments starting from "--" till the end of line
        String replacementRegexpNoEOL = "--(.*?)\$" //removes all sql comments starting from "--" till the end of file
        String cleanExpectedChangelog = expectedSqlChangelog
                .replaceAll(replacementRegexp, "")
                .replaceAll(replacementRegexpNoEOL, "")
                .trim()
        String cleanGeneratedChangelog = generatedSqlChangelog
                .replaceAll(replacementRegexp, "")
                .replaceAll(replacementRegexpNoEOL, "")
                .trim()
        assert cleanExpectedChangelog.equalsIgnoreCase(cleanGeneratedChangelog)
        uiService.sendMessage("GENERATED SQL CHANGELOG: \n $cleanGeneratedChangelog \n EXPECTED SQL CHANGELOG: \n $cleanExpectedChangelog")
    }

    static String getShortDatabaseName(String dbName) {
        switch (dbName) {
            case "percona-xtradb-cluster":
                return "mysql"
            case "db2-luw":
                return "db2"
            default:
                return dbName
        }
    }

    static String getSqlSpecificChangelogFile (String dbName, String changelogFileName) {
        def replacementName = String.format(".%s.sql", getShortDatabaseName(dbName))
        return changelogFileName.replace(".sql", replacementName)
    }

    static String removeSchemaNames(String generatedSql, Database database) {
        if (database.getShortName().equals("sqlite")) {
            return generatedSql.toLowerCase()
        }
        def schemaName = database.getDefaultSchemaName().toLowerCase()
        return generatedSql.toLowerCase().replace(schemaName + ".", "").replace("\"" + schemaName + "\".", "")
    }

    @Builder
    @ToString(includeNames = true, includeFields = true, includePackage = false, excludes = 'database,password')
    static class TestInput {
        String databaseName
        String version
        String username
        String password
        String url
        String setupChangelogPath
        String insertChangelogPath
        String updateChangelogPath
        String selectChangelogPath
        String xmlChangelogPath
        String jsonChangelogPath
        String ymlChangelogPath
        String sqlChangelogPath
        String dbSchema
        String change
        Database database
    }
}
