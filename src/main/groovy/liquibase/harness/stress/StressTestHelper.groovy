package liquibase.harness.stress

import groovy.transform.ToString
import groovy.transform.builder.Builder
import liquibase.Scope
import liquibase.database.Database
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.harness.util.FileUtils
import liquibase.ui.UIService

import java.nio.file.Path
import java.nio.file.Paths

class StressTestHelper {
    final static String baseChangelogPath = "liquibase/harness/generateChangelog/"

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
                            .inputChangelogFile(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                                    "expectedChangeLog", "xml").get(changeLogEntry.key))
                            .change(changeLogEntry.key)
                            .database(databaseUnderTest.database)
                            .build())
                }
            }
        }
        return inputList
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
        String inputChangelogFile
        String dbSchema
        String change
        Database database
    }
}
