package liquibase.harness.compatibility.advanced

import groovy.transform.ToString
import groovy.transform.builder.Builder
import liquibase.Scope
import liquibase.database.Database
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.harness.util.FileUtils
import liquibase.ui.UIService

class AdvancedTestHelper {
    final static String baseChangelogPath = "liquibase/harness/compatibility/advanced/"
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
                    "changelogs", "xml").entrySet()) {
                if (!commandLineChangesList || commandLineChangesList.contains(changeLogEntry.key)) {
                    inputList.add(TestInput.builder()
                            .databaseName(databaseUnderTest.name)
                            .url(databaseUnderTest.url)
                            .referenceUrl(databaseUnderTest.url.replace(databaseUnderTest.database.getDefaultSchemaName(), "secondarydb"))
                            .primaryDbSchemaName(databaseUnderTest.database.getDefaultSchemaName())
                            .username(databaseUnderTest.username)
                            .password(databaseUnderTest.password)
                            .version(databaseUnderTest.version)
                            .generatedResourcesPath(baseChangelogPath + "generatedChangelogs/" + changeLogEntry.key)
                            .diffResourcesPath(baseChangelogPath + "diffChangelogs/" + changeLogEntry.key)
                            .changelogPath(changeLogEntry.value)
                            .pathToExpectedDiffFile(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                                    "expectedDiff", "txt").get(changeLogEntry.key))
                            .pathToEmptyDiffFile(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                                    "expectedDiff", "txt").get("empty"))
                            .secondarySetupChangelogPath(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                                    "setup", "xml").get(changeLogEntry.key))
                            .secondaryDbScemaName("secondarydb")
                            .change(changeLogEntry.key)
                            .changeReversed(changeLogEntry.key.replace("create", "drop"))
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

    static String removeSchemaNames(String generatedSql, Database database, String schemaName) {
        if (database.getShortName().equals("sqlite")) {
            return generatedSql.toLowerCase()
        }
        return generatedSql.toLowerCase().replace(schemaName + ".", "").replace("\"" + schemaName + "\".", "")
    }

    static String cleanDiff(String diff) {
        String replacementRegexpRef = "Reference Database(.*?)\r?\n" //removes Reference Database diff line to generalize test data
        String replacementRegexpComp = "Comparison Database(.*?)\r?\n" //removes Comparison Database diff line to generalize test data
        String replacementRegexpWS = "\\s+" //removes whitespaces
        return diff
                .replaceAll(replacementRegexpRef, "")
                .replaceAll(replacementRegexpComp, "")
                .replaceAll(replacementRegexpWS, "")
    }

    static LinkedHashMap<String, String> configureChangelogMap(String basePath, String shortDbName) {
        def map = new LinkedHashMap<String, String>()
        map.put("xmlChangelog", basePath + ".xml")
        map.put("sqlChangelog", basePath + ".$shortDbName" + ".sql")
        map.put("ymlChangelog", basePath + ".yml")
        map.put("jsonChangelog", basePath + ".json")
        return map
    }

    @Builder
    @ToString(includeNames = true, includeFields = true, includePackage = false, excludes = 'database,password')
    static class TestInput {
        String databaseName
        String version
        String username
        String password
        String url
        String referenceUrl
        String generatedResourcesPath
        String diffResourcesPath
        String changelogPath
        String pathToExpectedDiffFile
        String pathToEmptyDiffFile
        String secondarySetupChangelogPath
        String primaryDbSchemaName
        String secondaryDbScemaName
        String change
        String changeReversed
        Database database
    }
}
