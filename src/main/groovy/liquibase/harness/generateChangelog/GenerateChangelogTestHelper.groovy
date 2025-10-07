package liquibase.harness.generateChangelog

import groovy.transform.ToString
import groovy.transform.builder.Builder
import liquibase.Scope
import liquibase.database.Database
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.harness.util.FileUtils
import liquibase.ui.UIService
import org.yaml.snakeyaml.Yaml

import java.nio.file.Path
import java.nio.file.Paths

class GenerateChangelogTestHelper {
    final static String baseChangelogPath = "liquibase/harness/generateChangelog/"
    final static UIService uiService = Scope.getCurrentScope().getUI()

    private static List<String> communityGenerateChangelogObjects
    private static List<String> secureGenerateChangelogObjects
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

        // Define generateChangelog objects based on their change types
        // Community: basic structures
        communityGenerateChangelogObjects = [
            'createTable', 'createView', 'createIndex',
            'addColumn', 'addPrimaryKey', 'addUniqueConstraint', 'addCheckConstraint'
        ]
        // Secure: advanced structures
        secureGenerateChangelogObjects = [
            'createSequence', 'createFunction', 'createProcedure', 'createTrigger',
            'createPackage', 'createPackageBody', 'createSynonym', 'addForeignKey'
        ]
    }

    static List<TestInput> buildTestInput() {
        String commandLineChanges = System.getProperty("change")
        String testMode = System.getProperty("testMode") // 'secure', 'community', or null (all tests)

        List commandLineChangesList = Collections.emptyList()
        if (commandLineChanges) {
            commandLineChangesList = Arrays.asList(commandLineChanges.contains(",")
                    ? commandLineChanges.split(",")
                    : commandLineChanges)
        }

        // Determine which generateChangelog objects to test based on testMode
        List<String> allowedChangelogObjects = []
        String modeMessage = ""

        if (testMode == "secure") {
            allowedChangelogObjects = secureGenerateChangelogObjects
            modeMessage = "Running Secure generateChangelog tests"
        } else if (testMode == "community") {
            allowedChangelogObjects = communityGenerateChangelogObjects
            modeMessage = "Running Community generateChangelog tests"
        } else {
            // Run all tests (both community and secure) - testMode is null, empty, or "all"
            allowedChangelogObjects = communityGenerateChangelogObjects + secureGenerateChangelogObjects
            modeMessage = "Running all generateChangelog tests (Community + Secure)"
        }

        uiService.sendMessage(modeMessage)

        List<TestInput> inputList = new ArrayList<>()
        DatabaseConnectionUtil databaseConnectionUtil = new DatabaseConnectionUtil()
        for (DatabaseUnderTest databaseUnderTest : databaseConnectionUtil
                .initializeDatabasesConnection(TestConfig.instance.getFilteredDatabasesUnderTest())) {
            for (def changeLogEntry : FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                    "expectedChangeLog", "xml").entrySet()) {
                // Filter based on testMode and commandLine filters
                if (allowedChangelogObjects.contains(changeLogEntry.key) &&
                    (!commandLineChangesList || commandLineChangesList.contains(changeLogEntry.key))) {
                    inputList.add(TestInput.builder()
                            .databaseName(databaseUnderTest.name)
                            .url(databaseUnderTest.url)
                            .username(databaseUnderTest.username)
                            .password(databaseUnderTest.password)
                            .version(databaseUnderTest.version)
                            .inputChangelogFile(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                                    "expectedChangeLog", "xml").get(changeLogEntry.key))
                            .expectedSqlPath(FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath +
                                    "expectedSql", "sql").get(changeLogEntry.key))
                            .change(changeLogEntry.key)
                            .database(databaseUnderTest.database)
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

    static void clearFolder(String pathToObjectDir) {
        Path path = Paths.get(pathToObjectDir)
        if (path.toFile().isDirectory()) {
            org.apache.commons.io.FileUtils.forceDelete(new File(pathToObjectDir))
        }
    }

    static String removeSchemaNames(String generatedSql, Database database) {
        if (database.getShortName().equals("sqlite")) {
            return generatedSql.toLowerCase()
        }
        def schemaName = database.getDefaultSchemaName().toLowerCase()
        return generatedSql.toLowerCase().replace(schemaName + ".", "").replace("\"" + schemaName + "\".", "")
    }

    @Builder
    @ToString(includeNames = true, includeFields = true, includePackage = false, excludes = 'database,password')
    static class TestInput {
        String databaseName
        String version
        String username
        String password
        String url
        String inputChangelogFile
        String expectedSqlPath
        String change
        Database database
    }
}
