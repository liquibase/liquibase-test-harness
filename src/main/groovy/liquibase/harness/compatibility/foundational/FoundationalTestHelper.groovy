package liquibase.harness.compatibility.foundational

import groovy.transform.ToString
import groovy.transform.builder.Builder
import liquibase.Scope
import liquibase.database.Database
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.harness.util.FileUtils
import liquibase.ui.UIService

class FoundationalTestHelper {
    final static String baseChangelogPath = "liquibase/harness/compatibility/foundational/"
    final static UIService uiService = Scope.getCurrentScope().getUI()

    static List<TestInput> buildTestInput() {
        List<TestInput> inputList = new ArrayList<>()
        DatabaseConnectionUtil databaseConnectionUtil = new DatabaseConnectionUtil()
        for (DatabaseUnderTest databaseUnderTest : databaseConnectionUtil
                .initializeDatabasesConnection(TestConfig.instance.getFilteredDatabasesUnderTest())) {
            for (def changeLogEntry : FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                    "stress/setup", "xml").entrySet()) {
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
                        .expectedXmlChangelogPath(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                                "expectedChangeLog", "xml").get(changeLogEntry.key))
                        .expectedSqlChangelogPath(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                                "expectedChangeLog", "sql").get(changeLogEntry.key))
                        .expectedYmlChangelogPath(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                                "expectedChangeLog", "yml").get(changeLogEntry.key))
                        .expectedJsonChangelogPath(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                                "expectedChangeLog", "json").get(changeLogEntry.key))
                        .change(changeLogEntry.key)
                        .database(databaseUnderTest.database)
                        .build())
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
        String expectedXmlChangelogPath
        String expectedSqlChangelogPath
        String expectedYmlChangelogPath
        String expectedJsonChangelogPath
        String dbSchema
        String change
        Database database
    }
}
