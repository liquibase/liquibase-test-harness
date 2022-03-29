package liquibase.harness.snapshot

import groovy.transform.ToString
import groovy.transform.builder.Builder
import liquibase.Scope
import liquibase.database.Database
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.harness.util.FileUtils
import liquibase.statement.SqlStatement
import liquibase.statement.core.RawSqlStatement
import org.junit.Assert
import java.sql.Connection
import java.sql.DriverManager
import java.util.logging.Logger

class SnapshotObjectTestHelper {

    final static String baseSnapshotPath = "liquibase/harness/snapshot/"
    final static String inputSqlPath = "${baseSnapshotPath}inputSql"
    final static String cleanupSqlPath = "${baseSnapshotPath}cleanupSql"
    final static String expectedSnapshotPath = "${baseSnapshotPath}expectedSnapshot"

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
        for (DatabaseUnderTest databaseUnderTest: new DatabaseConnectionUtil()
                .initializeDatabasesConnection(TestConfig.instance.getFilteredDatabasesUnderTest())) {
            for (def changeLogEntry : FileUtils.resolveInputFilePaths(databaseUnderTest, inputSqlPath, "sql")
                    .entrySet()) {
                if (!commandLineSnapshotObjectList || commandLineSnapshotObjectList.contains(changeLogEntry.key)) {
                    String pathToCleanupSQL = FileUtils.resolveInputFilePaths(databaseUnderTest,
                            cleanupSqlPath, "sql").get(changeLogEntry.key)
                    String pathToExpectedSnapshot = FileUtils.resolveInputFilePaths(databaseUnderTest,
                                    expectedSnapshotPath, "json").get(changeLogEntry.key)
                    inputList.add(TestInput.builder()
                            .database(databaseUnderTest)
                            .snapshotObjectName(changeLogEntry.key)
                            .pathToInputSql("/${changeLogEntry.value}")
                            .pathToCleanupSql("/${pathToCleanupSQL}")
                            .pathToExpectedSnapshotFile("/${pathToExpectedSnapshot}")
                            .build())
                }
            }
        }
        return inputList
    }

    static void executeQuery(String pathToSql, TestInput testInput) {
        String query = FileUtils.getResourceContent(pathToSql)
        Database database = testInput.database.database
        Connection newConnection
        if (database.connection.isClosed()) {
            try {
                newConnection = DriverManager.getConnection(testInput.database.url, testInput.database.username,
                        testInput.database.password)
                newConnection.createStatement().execute(query)
                newConnection.commit()
            } catch (Exception exception) {
                Scope.getCurrentScope().getUI().sendMessage("Failed to execute query! " + query + " " +
                        exception.printStackTrace())
                Assert.fail exception.message
            } finally {
                if (newConnection != null) {
                    newConnection.close()
                }
            }
        }
        try {
            database.execute([new RawSqlStatement(query)] as SqlStatement[], null)
            database.commit()
        } catch (Exception exception)  {
            Scope.getCurrentScope().getUI().sendMessage("Failed to execute query! " + query + " " +
                    exception.printStackTrace())
            Assert.fail exception.message
        }
    }

    @Builder
    @ToString(includeNames=true, includeFields=true, includePackage = false)
    static class TestInput {
        DatabaseUnderTest database
        String snapshotObjectName
        String pathToInputSql
        String pathToCleanupSql
        String pathToExpectedSnapshotFile
    }
}
