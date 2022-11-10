package liquibase.harness.compatibility.foundational

import groovy.transform.ToString
import groovy.transform.builder.Builder
import liquibase.database.Database
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.harness.util.FileUtils

class FoundationalTestHelper {
    final static String baseChangelogPath = "liquibase/harness/compatibility/foundational/"

    static List<TestInput> buildTestInput() {
        List<TestInput> inputList = new ArrayList<>()
        DatabaseConnectionUtil databaseConnectionUtil = new DatabaseConnectionUtil()
        for (DatabaseUnderTest databaseUnderTest : databaseConnectionUtil
                .initializeDatabasesConnection(TestConfig.instance.getFilteredDatabasesUnderTest())) {
            for (def changeLogEntry : FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath + "setup", "xml").entrySet()) {
                inputList.add(TestInput.builder()
                        .databaseName(databaseUnderTest.name)
                        .url(databaseUnderTest.url)
                        .dbSchema(databaseUnderTest.dbSchema)
                        .username(databaseUnderTest.username)
                        .password(databaseUnderTest.password)
                        .version(databaseUnderTest.version)
                        .setupChangelogPath(changeLogEntry.value)
                        .insertChangelogPath(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath + "insert", "xml").get(changeLogEntry.key))
                        .updateChangelogPath(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath + "update", "xml").get(changeLogEntry.key))
                        .selectChangelogPath(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath + "select", "xml").get(changeLogEntry.key))
                        .change(changeLogEntry.key)
                        .database(databaseUnderTest.database)
                        .build())
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
        String dbSchema
        String change
        Database database
    }
}
