package liquibase.harness.snapshot

import groovy.transform.ToString
import groovy.transform.builder.Builder
import liquibase.database.Database
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.harness.util.FileUtils
import java.util.logging.Logger

class SnapshotObjectTestHelper {

    final static String baseSnapshotPath = "liquibase/harness/snapshot/"

    static List<TestInput> buildTestInput() {
        String commandLineSnapshotObjects = System.getProperty("snapshotObjects")
        List commandLineSnapshotObjectList = Collections.emptyList()
        if (commandLineSnapshotObjects) {
            commandLineSnapshotObjectList = Arrays.asList(commandLineSnapshotObjects.contains(",")
                    ? commandLineSnapshotObjects.split(",")
                    : commandLineSnapshotObjects)
        }
        Logger.getLogger(this.class.name).warning("Only " + TestConfig.instance.inputFormat
                + " input files are taken into account for this test run")
        List<TestInput> inputList = new ArrayList<>()
        for (DatabaseUnderTest databaseUnderTest : new DatabaseConnectionUtil()
                .initializeDatabasesConnection(TestConfig.instance.getFilteredDatabasesUnderTest())) {
            for (def changeLogEntry : FileUtils.resolveInputFilePaths(databaseUnderTest, baseSnapshotPath + "changelogs", "xml")
                    .entrySet()) {
                if (!commandLineSnapshotObjectList || commandLineSnapshotObjectList.contains(changeLogEntry.key)) {
                    String pathToExpectedSnapshot = FileUtils.resolveInputFilePaths(databaseUnderTest,
                            baseSnapshotPath + "expectedSnapshot", "json").get(changeLogEntry.key)
                    inputList.add(TestInput.builder()
                            .database(databaseUnderTest.database)
                            .databaseName(databaseUnderTest.name)
                            .databaseVersion(databaseUnderTest.version)
                            .url(databaseUnderTest.url)
                            .username(databaseUnderTest.username)
                            .password(databaseUnderTest.password)
                            .snapshotObject(changeLogEntry.key)
                            .pathToChangelogFile("/${changeLogEntry.value}")
                            .pathToExpectedSnapshotFile("/${pathToExpectedSnapshot}")
                            .build())
                }
            }
        }
        return inputList
    }

    @Builder
    @ToString(includeNames = true, includeFields = true, includePackage = false, excludes ='database')
    static class TestInput {
        Database database
        String databaseName
        String databaseVersion
        String url
        String username
        String password
        String snapshotObject
        String pathToChangelogFile
        String pathToExpectedSnapshotFile
    }
}
