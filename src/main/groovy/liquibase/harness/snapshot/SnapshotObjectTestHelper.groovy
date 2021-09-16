package liquibase.harness.snapshot

import groovy.transform.builder.Builder
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.harness.util.FileUtils
import java.util.logging.Logger

class SnapshotObjectTestHelper {

    final static String baseChangelogPath = "liquibase/harness/snapshot/changelog"
    final static String baseExpectedSnapshotPath = "/liquibase/harness/snapshot/expectedSnapshot"

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
        DatabaseConnectionUtil databaseConnectionUtil = new DatabaseConnectionUtil()

        for (DatabaseUnderTest databaseUnderTest : databaseConnectionUtil
                .initializeDatabasesConnection(TestConfig.instance.databasesUnderTest)) {
            for (def changeLogEntry : FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath,
                    TestConfig.instance.inputFormat).entrySet()) {
                if (!commandLineSnapshotObjectList || commandLineSnapshotObjectList.contains(changeLogEntry.key)) {
                    inputList.add(TestInput.builder()
                            .database(databaseUnderTest)
                            .snapshotObjectName(changeLogEntry.key)
                            .pathToChangelogFile(changeLogEntry.value)
                            .pathToExpectedSnapshotFile("${baseExpectedSnapshotPath}/${changeLogEntry.key}.json")
                            .build())
                }
            }
        }
        return inputList
    }

    @Builder
    static class TestInput {
        DatabaseUnderTest database
        String snapshotObjectName
        String pathToChangelogFile
        String pathToExpectedSnapshotFile
    }
}
