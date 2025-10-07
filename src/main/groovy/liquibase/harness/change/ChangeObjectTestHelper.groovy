package liquibase.harness.change

import groovy.transform.ToString
import groovy.transform.builder.Builder
import liquibase.Scope
import liquibase.database.Database
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.harness.util.FileUtils
import org.yaml.snakeyaml.Yaml

class ChangeObjectTestHelper {

    final static List supportedChangeLogFormats = ['xml', 'sql', 'json', 'yml', 'yaml'].asImmutable()
    final static String baseChangelogPath = "liquibase/harness/change/changelogs"

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
    }

    static List<TestInput> buildTestInput() {
        String commandLineInputFormat = System.getProperty("inputFormat")
        String commandLineChangeObjects = System.getProperty("changeObjects")
        String testMode = System.getProperty("testMode") // 'secure', 'community', or null (all tests)

        List commandLineChangeObjectList = Collections.emptyList()
        if (commandLineChangeObjects) {
            commandLineChangeObjectList = Arrays.asList(commandLineChangeObjects.contains(",")
                    ? commandLineChangeObjects.split(",")
                    : commandLineChangeObjects)
        }
        if (commandLineInputFormat) {
            if (!supportedChangeLogFormats.contains(commandLineInputFormat)) {
                throw new IllegalArgumentException(commandLineInputFormat + " inputFormat is not supported")
            }
            TestConfig.instance.inputFormat = commandLineInputFormat
        }

        // Determine which changetypes to test based on testMode
        List<String> allowedChangetypes = []
        String modeMessage = ""

        if (testMode == "secure") {
            allowedChangetypes = secureChangetypes
            modeMessage = "Running Secure changetype tests"
        } else if (testMode == "community") {
            allowedChangetypes = communityChangetypes
            modeMessage = "Running Community changetype tests"
        } else {
            // Run all tests (both community and secure) - testMode is null, empty, or "all"
            allowedChangetypes = communityChangetypes + secureChangetypes
            modeMessage = "Running all changetype tests (Community + Secure)"
        }

        Scope.getCurrentScope().getUI().sendMessage(modeMessage + " with " + TestConfig.instance.inputFormat
                + " input files")

        List<TestInput> inputList = new ArrayList<>()
        DatabaseConnectionUtil databaseConnectionUtil = new DatabaseConnectionUtil()

        for (DatabaseUnderTest databaseUnderTest : databaseConnectionUtil
                .initializeDatabasesConnection(TestConfig.instance.getFilteredDatabasesUnderTest())) {
            def database = databaseUnderTest.database
            for (def changeLogEntry : FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath,
                    TestConfig.instance.inputFormat).entrySet()) {
                // Filter based on testMode and commandLine filters
                if (allowedChangetypes.contains(changeLogEntry.key) &&
                    (!commandLineChangeObjectList || commandLineChangeObjectList.contains(changeLogEntry.key))) {
                    inputList.add(TestInput.builder()
                            .databaseName(databaseUnderTest.name)
                            .url(databaseUnderTest.url)
                            .dbSchema(databaseUnderTest.dbSchema)
                            .username(databaseUnderTest.username)
                            .password(databaseUnderTest.password)
                            .version(databaseUnderTest.version)
                            .context(TestConfig.instance.context)
                            .changeObject(changeLogEntry.key)
                            .pathToChangeLogFile(changeLogEntry.value)
                            .database(database)
                            .build())
                }
            }
        }
        return inputList
    }

    static void saveAsExpectedSql(String generatedSql, TestInput testInput) {
        File outputFile = "${TestConfig.instance.outputResourcesBase}/liquibase/harness/change/expectedSql/" +
                "${testInput.databaseName}/${testInput.changeObject}.sql" as File
        outputFile.parentFile.mkdirs()
        try {
            outputFile.write(generatedSql)
        } catch (IOException exception) {
            Scope.getCurrentScope().getUI().sendErrorMessage("Failed to save generated sql file! " + exception.message)
        }
    }

    @Builder
    @ToString(includeNames = true, includeFields = true, includePackage = false, excludes = 'database,password')
    static class TestInput {
        String databaseName
        String url
        String dbSchema
        String username
        String password
        String version
        String context
        String changeObject
        String pathToChangeLogFile
        Database database
    }
}
