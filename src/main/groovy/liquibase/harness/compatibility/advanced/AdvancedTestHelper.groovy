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
                                    "setup", "xml").get(changeLogEntry.key))
                            .generateCLResourcesPath(baseChangelogPath + "generatedChangelogs/" + changeLogEntry.key)
                            .diffCLResourcesPath(baseChangelogPath + "diffChangelogs/" + changeLogEntry.key)
                            .pathToExpectedDiffFile(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                                    "expectedDiff", "txt").get(changeLogEntry.key))
                            .pathToEmptyDiffFile(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                                    "expectedDiff", "txt").get("empty"))
                            .change(changeLogEntry.key)
                            .changeReversed(changeLogEntry.key.replace("create", "drop").replace("add", "drop"))
                            .expectedSnapshot(FileUtils.getJSONFileContent(changeLogEntry.key, databaseUnderTest.name, databaseUnderTest.version,
                                    baseChangelogPath + "expectedSnapshot"))
                            .verificationGenCLSql(parseQuery(getSqlFileContent(changeLogEntry.key, databaseUnderTest.name, databaseUnderTest.version,
                                    baseChangelogPath + "verificationSql/generateChangelog")))
                            .verificationDiffCLSql(parseQuery(getSqlFileContent(changeLogEntry.key, databaseUnderTest.name, databaseUnderTest.version,
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

    static validateChangelog( String changelogFormat, String changelogContent, String verificationSql, String change, String changeReversed) {
        if (changelogFormat.equalsIgnoreCase("sqlChangelog")) {
            validateSqlChangelog(verificationSql, changelogContent)
        } else {
            assert changelogContent.contains("$change")
            if (changeReversed != null) {
                assert changelogContent.contains("$changeReversed")
            }
        }
    }

    static String getChangelogValidationSql(String searchPath, String change, String dbName, String dbVersion) {
        def validationSql
        try {
            validationSql = parseQuery(getSqlFileContent(change, dbName, dbVersion, baseChangelogPath + "expectedSql/" + searchPath)).toLowerCase()
        } catch (NullPointerException exception) {
            validationSql = parseQuery(getSqlFileContent(change, dbName, dbVersion, baseChangelogPath + "verificationSql/" + searchPath)).toLowerCase()
        }
        return validationSql
    }

    static validateSql(String generatedSql, String expectedSql) {
        def message
        if (generatedSql == expectedSql) {
            message = "Generated sql is correct!"
        } else {
            message ="FAIL! Expected sql doesn't match generated sql! \nEXPECTED SQL: \n" + expectedSql + " \n" + "GENERATED SQL: \n" + generatedSql
        }
        Scope.getCurrentScope().getUI().sendMessage(message)
        generatedSql == expectedSql
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
        String generateCLResourcesPath
        String diffCLResourcesPath
        String primarySetupChangelogPath
        String secondarySetupChangelogPath
        String pathToExpectedDiffFile
        String pathToEmptyDiffFile
        String primaryDbSchemaName
        String secondaryDbSchemaName
        String change
        String changeReversed
        String expectedSnapshot
        String verificationGenCLSql
        String verificationDiffCLSql
        Database database
    }
}
