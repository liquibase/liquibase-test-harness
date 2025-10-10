package liquibase.harness.snapshot

import groovy.transform.ToString
import groovy.transform.builder.Builder
import liquibase.database.Database
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.harness.util.FileUtils
import org.yaml.snakeyaml.Yaml
import java.util.logging.Logger

class SnapshotObjectTestHelper {

    final static String baseSnapshotPath = "liquibase/harness/snapshot/"

    private static List<String> communitySnapshotObjects
    private static List<String> secureSnapshotObjects
    private static List<String> communityChangetypes
    private static List<String> secureChangetypes

    static {
        // Load changetypes from the YAML file
        def yamlFile = new File("supported-changetypes.yml")
        if (yamlFile.exists()) {
            Yaml yaml = new Yaml()
            def config = yaml.load(yamlFile.text)
            communityChangetypes = config.community_changetypes
            secureChangetypes = config.secure_changetypes
        } else {
            // Fallback to hardcoded lists if file doesn't exist
            communityChangetypes = [
                'createTable', 'createTableDataTypeText', 'createTableTimestamp', 'dropTable', 'renameTable',
                'addColumn', 'dropColumn', 'renameColumn',
                'createIndex', 'dropIndex',
                'createView', 'dropView',
                'addCheckConstraint', 'addDefaultValue', 'addDefaultValueBoolean', 'addDefaultValueNumeric',
                'addDefaultValueDate', 'addNotNullConstraint', 'addPrimaryKey', 'addUniqueConstraint',
                'dropCheckConstraint', 'dropDefaultValue', 'dropNotNullConstraint', 'dropPrimaryKey', 'dropUniqueConstraint',
                'sql', 'sqlFile'
            ]
            secureChangetypes = [
                'setTableRemarks', 'setColumnRemarks', 'addAutoIncrement',
                'createSequence', 'dropSequence', 'alterSequence', 'renameSequence', 'addDefaultValueSequenceNext',
                'createFunction', 'dropFunction', 'createProcedure', 'dropProcedure', 'createProcedureFromFile',
                'createPackage', 'createPackageBody',
                'createTrigger', 'dropTrigger', 'disableTrigger', 'enableTrigger', 'renameTrigger',
                'addForeignKey', 'dropForeignKey', 'dropAllForeignKeyConstraints',
                'disableCheckConstraint', 'enableCheckConstraint',
                'addLookupTable', 'mergeColumns', 'modifyDataType',
                'addDefaultValueComputed', 'modifySql', 'executeCommand', 'renameView'
            ]
        }

        // Define snapshot objects based on their change types
        // Community: basic structures
        communitySnapshotObjects = [
            'createTable', 'createView', 'createIndex',
            'addColumn', 'addPrimaryKey', 'addUniqueConstraint',
            'snapshotCatalogAndSchema'
        ]
        // Secure: advanced structures - include items that are in secure changetypes
        secureSnapshotObjects = [
            'createSequence', 'addForeignKeyConstraint'
        ]
    }

    static List<TestInput> buildTestInput() {
        String commandLineSnapshotObjects = System.getProperty("snapshotObjects")
        String testMode = System.getProperty("testMode") // 'secure', 'community', or null (all tests)

        List commandLineSnapshotObjectList = Collections.emptyList()
        if (commandLineSnapshotObjects) {
            commandLineSnapshotObjectList = Arrays.asList(commandLineSnapshotObjects.contains(",")
                    ? commandLineSnapshotObjects.split(",")
                    : commandLineSnapshotObjects)
        }

        // Determine which snapshot objects to test based on testMode
        List<String> allowedSnapshotObjects = []
        String modeMessage = ""

        if (testMode == "secure") {
            allowedSnapshotObjects = secureSnapshotObjects
            modeMessage = "Running Secure snapshot object tests"
        } else if (testMode == "community") {
            allowedSnapshotObjects = communitySnapshotObjects
            modeMessage = "Running Community snapshot object tests"
        } else {
            // Run all tests (both community and secure) - testMode is null, empty, or "all"
            allowedSnapshotObjects = communitySnapshotObjects + secureSnapshotObjects
            modeMessage = "Running all snapshot object tests (Community + Secure)"
        }

        Logger.getLogger(this.class.name).warning(modeMessage + " with " + TestConfig.instance.inputFormat
                + " input files")

        List<TestInput> inputList = new ArrayList<>()
        for (DatabaseUnderTest databaseUnderTest : new DatabaseConnectionUtil()
                .initializeDatabasesConnection(TestConfig.instance.getFilteredDatabasesUnderTest())) {
            for (def changeLogEntry : FileUtils.resolveInputFilePaths(databaseUnderTest, baseSnapshotPath + "changelogs", "xml")
                    .entrySet()) {
                // Filter based on testMode and commandLine filters
                if (allowedSnapshotObjects.contains(changeLogEntry.key) &&
                    (!commandLineSnapshotObjectList || commandLineSnapshotObjectList.contains(changeLogEntry.key))) {
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
