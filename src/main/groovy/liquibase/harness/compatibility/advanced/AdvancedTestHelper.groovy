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

import static liquibase.harness.util.FileUtils.getSqlFileContent
import static liquibase.harness.util.TestUtils.parseQuery

class AdvancedTestHelper {
    final static String baseChangelogPath = "liquibase/harness/compatibility/advanced/"
    final static UIService uiService = Scope.getCurrentScope().getUI()
    final static String secondaryDbName = "secondarydb"

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
                            .database(databaseUnderTest.database)
                            .databaseName(databaseUnderTest.name)
                            .url(databaseUnderTest.url)
                            .referenceUrl(databaseUnderTest.url.replace(databaseUnderTest.database.getDefaultSchemaName(), secondaryDbName))
                            .primaryDbSchemaName(databaseUnderTest.database.getDefaultSchemaName())
                            .secondaryDbSchemaName(secondaryDbName)
                            .username(databaseUnderTest.username)
                            .password(databaseUnderTest.password)
                            .version(databaseUnderTest.version)
                            .primarySetupChangelogPath(changeLogEntry.value)
                            .secondarySetupChangelogPath(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                                    "secondaryInstanceChangelogs", "xml").get(changeLogEntry.key))
                            .generateChangelogResourcesPath(baseChangelogPath + "generatedChangelogs/" + changeLogEntry.key)
                            .diffChangelogResourcesPath(baseChangelogPath + "diffChangelogs/" + changeLogEntry.key)
                            .pathToExpectedDiffFile(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                                    "expectedDiff", "txt").get(changeLogEntry.key))
                            .pathToEmptyDiffFile(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                                    "expectedDiff", "txt").get("empty"))
                            .change(changeLogEntry.key)
                            .changeReversed(changeLogEntry.key.replace("create", "drop").replace("add", "drop"))
                            .expectedSnapshot(FileUtils.getJSONFileContent(changeLogEntry.key, databaseUnderTest.name, databaseUnderTest.version,
                                    baseChangelogPath + "expectedSnapshot"))
                            .verificationSetupSql(parseQuery(getSqlFileContent(changeLogEntry.key, databaseUnderTest.name, databaseUnderTest.version,
                                    baseChangelogPath + "verificationSql/setup")))
                            .verificationGenerateChangelogSql(parseQuery(getSqlFileContent(changeLogEntry.key, databaseUnderTest.name, databaseUnderTest.version,
                                    baseChangelogPath + "verificationSql/generateChangelog")))
                            .verificationDiffChangelogSql(parseQuery(getSqlFileContent(changeLogEntry.key, databaseUnderTest.name, databaseUnderTest.version,
                                    baseChangelogPath + "verificationSql/diffChangelog")))
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
        def message
        if (cleanExpectedChangelog.equalsIgnoreCase(cleanGeneratedChangelog)) {
            message = "GENERATED SQL CHANGELOG IS CORRECT"
        } else {
            message = "GENERATED SQL CHANGELOG: \n $cleanGeneratedChangelog \n DOES NOT MATCH EXPECTED SQL CHANGELOG: \n $cleanExpectedChangelog"
        }
        uiService.sendMessage(message)
        assert cleanExpectedChangelog.equalsIgnoreCase(cleanGeneratedChangelog)
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

    static String removeDatabaseInfoFromDiff(String diff) {
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

    static validateGenerateChangelog(String changelogFormat, String changelogContent, String verificationSql, String change) {
        if (changelogFormat.equalsIgnoreCase("sqlChangelog")) {
            validateSqlChangelog(verificationSql, changelogContent)
        } else {
            def message = changelogContent.contains("$change") ? "GENERATED CHANGELOG CONTAINS $change CHANGE" :
                    "FAIL! GENERATED CHANGELOG DOES NOT CONTAIN $change CHANGE"
            uiService.sendMessage(message)
            assert changelogContent.contains("$change")
        }
    }

    static validateDiffChangelog(String changelogFormat, String changelogContent, String verificationSql, String change, String changeReversed) {
        validateGenerateChangelog(changelogFormat, changelogContent, verificationSql, change)
        if (!changelogFormat.equalsIgnoreCase("sqlChangelog")) {
            def message = changelogContent.contains("$change") ? "GENERATED CHANGELOG CONTAINS $changeReversed CHANGE" :
                    "FAIL! GENERATED CHANGELOG DOES NOT CONTAIN $changeReversed CHANGE"
            uiService.sendMessage(message)
            assert changelogContent.contains("$changeReversed")
        }
    }

    static String getChangelogValidationSql(String searchPath, String change, String dbName, String dbVersion) {
//        def validationSql
//        try {
//            validationSql = parseQuery(getSqlFileContent(change, dbName, dbVersion, baseChangelogPath + "expectedSql/" + searchPath)).toLowerCase()
//        } catch (NullPointerException exception) {
//            validationSql = parseQuery(getSqlFileContent(change, dbName, dbVersion, baseChangelogPath + "verificationSql/" + searchPath)).toLowerCase()
//        }
        return parseQuery(getSqlFileContent(change, dbName, dbVersion, baseChangelogPath + "verificationSql/" + searchPath)).toLowerCase()
    }

    static validateSql(String generatedSql, String expectedSql) {
        def message
        if (generatedSql == expectedSql) {
            message = "GENERATED SQL IS CORRECT"
        } else {
            message ="FAIL! Expected sql doesn't match generated sql! \nEXPECTED SQL: \n" + expectedSql + " \n" + "GENERATED SQL: \n" + generatedSql
        }
        Scope.getCurrentScope().getUI().sendMessage(message)
        generatedSql == expectedSql
    }

    static validateDiff(String generatedDiff, String expectedDiff) {
        def message
        if (generatedDiff == expectedDiff) {
            message = "GENERATED DIFF IS CORRECT"
        } else {
            message ="FAIL! EXPECTED DIFF DOESN'T MATCH GENERATED DIFF!"
        }
        uiService.sendMessage(message)
        generatedDiff == expectedDiff
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
        String generateChangelogResourcesPath
        String diffChangelogResourcesPath
        String primarySetupChangelogPath
        String secondarySetupChangelogPath
        String pathToExpectedDiffFile
        String pathToEmptyDiffFile
        String primaryDbSchemaName
        String secondaryDbSchemaName
        String change
        String changeReversed
        String expectedSnapshot
        String verificationSetupSql
        String verificationGenerateChangelogSql
        String verificationDiffChangelogSql
        Database database
    }
}
