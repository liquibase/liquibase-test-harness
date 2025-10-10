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
import org.yaml.snakeyaml.Yaml

import static liquibase.harness.util.FileUtils.getSqlFileContent
import static liquibase.harness.util.TestUtils.parseQuery

class AdvancedTestHelper {
    final static String baseResourcePath = "liquibase/harness/compatibility/advanced/"
    final static UIService uiService = Scope.getCurrentScope().getUI()
    final static String secondaryDbName = "secondarydb"

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
        String commandLineChanges = System.getProperty("change")
        String testMode = System.getProperty("testMode") // 'secure', 'community', or null (all tests)

        List commandLineChangesList = Collections.emptyList()
        if (commandLineChanges) {
            commandLineChangesList = Arrays.asList(commandLineChanges.contains(",")
                    ? commandLineChanges.split(",")
                    : commandLineChanges)
        }

        // Determine which changetypes to test based on testMode
        List<String> allowedChangetypes = []
        String modeMessage = ""

        if (testMode == "secure") {
            allowedChangetypes = secureChangetypes
            modeMessage = "Running Secure advanced tests"
        } else if (testMode == "community") {
            allowedChangetypes = communityChangetypes
            modeMessage = "Running Community advanced tests"
        } else {
            // Run all tests (both community and secure) - testMode is null, empty, or "all"
            allowedChangetypes = communityChangetypes + secureChangetypes
            modeMessage = "Running all advanced tests (Community + Secure)"
        }

        uiService.sendMessage(modeMessage)

        List<TestInput> inputList = new ArrayList<>()
        DatabaseConnectionUtil databaseConnectionUtil = new DatabaseConnectionUtil()
        for (DatabaseUnderTest databaseUnderTest : databaseConnectionUtil
                .initializeDatabasesConnection(TestConfig.instance.getFilteredDatabasesUnderTest())) {
            for (def changeUnderTest : FileUtils.resolveInputFilePaths(databaseUnderTest, baseResourcePath +
                    "initSql/primary", "sql").entrySet()) {
                // Filter based on testMode and commandLine filters
                // Handle special cases for test names that map to changetypes
                def changeType = changeUnderTest.key
                if (changeType == "column") {
                    changeType = "addColumn"
                } else if (changeType == "addForeignKeyConstraint") {
                    changeType = "addForeignKey"
                }

                if (allowedChangetypes.contains(changeType) &&
                    (!commandLineChangesList || commandLineChangesList.contains(changeUnderTest.key))) {
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
                            .primaryInitSqlPath(changeUnderTest.value)
                            .secondaryInitSqlPath(FileUtils.resolveInputFilePaths(databaseUnderTest, baseResourcePath +
                                    "initSql/secondary", "sql").get(changeUnderTest.key))
                            .generateChangelogResourcesPath(baseResourcePath + "generatedChangelogs/" + changeUnderTest.key)
                            .diffChangelogResourcesPath(baseResourcePath + "diffChangelogs/" + changeUnderTest.key)
                            .pathToExpectedDiffFile(FileUtils.resolveInputFilePaths(databaseUnderTest, baseResourcePath +
                                    "expectedDiff", "txt").get(changeUnderTest.key))
                            .pathToEmptyDiffFile(FileUtils.resolveInputFilePaths(databaseUnderTest, baseResourcePath +
                                    "expectedDiff", "txt").get("empty"))
                            .change(changeUnderTest.key)
                            .changeReversed(changeUnderTest.key.replace("create", "drop").replace("add", "drop"))
                            .expectedSnapshot(FileUtils.getJSONFileContent(changeUnderTest.key, databaseUnderTest.name, databaseUnderTest.version,
                                    baseResourcePath + "expectedSnapshot"))
                            .expectedGenerateChangelogSql(parseQuery(getSqlFileContent(changeUnderTest.key, databaseUnderTest.name, databaseUnderTest.version,
                                    baseResourcePath + "expectedSql/generateChangelog")))
                            .expectedDiffChangelogSql(parseQuery(getSqlFileContent(changeUnderTest.key, databaseUnderTest.name, databaseUnderTest.version,
                                    baseResourcePath + "expectedSql/diffChangelog")))
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
        def message
        if (cleanExpectedChangelog.equalsIgnoreCase(cleanGeneratedChangelog)) {
            message = "GENERATED SQL CHANGELOG IS CORRECT"
        } else {
            message = "GENERATED SQL CHANGELOG: \n $cleanGeneratedChangelog \n DOES NOT MATCH EXPECTED SQL CHANGELOG: \n $cleanExpectedChangelog"
        }
        uiService.sendMessage(message)
        assert cleanExpectedChangelog.equalsIgnoreCase(cleanGeneratedChangelog)
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

    static String removeDatabaseInfoFromDiff(String diff) {
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

    static validateGenerateChangelog(String changelogFormat, String changelogContent, String expectedSql, String change, String commandName) {
        if (changelogFormat.equalsIgnoreCase("sqlChangelog")) {
            if (expectedSql == null) {
                uiService.sendMessage("WARNING! No expectedSql was found! The test will auto-generate new expectedSql file to\n" +
                        "src/test/resources/liquibase/harness/compatibility/advanced/expectedSql/${commandName}\n" +
                        "folder. Please verify its content and use it as expectedSql test data.")
            } else {
                validateSqlChangelog(expectedSql, changelogContent)
            }
        } else {
            uiService.sendMessage(changelogContent.contains("$change") ? "GENERATED CHANGELOG CONTAINS $change CHANGE" :
                    "FAIL! GENERATED CHANGELOG DOES NOT CONTAIN $change CHANGE")
            assert changelogContent.contains("$change")
        }
    }

    static validateDiffChangelog(String changelogFormat, String changelogContent, String expectedSql, String change, String changeReversed, String commandName) {
        validateGenerateChangelog(changelogFormat, changelogContent, expectedSql, change, commandName)
        if (!changelogFormat.equalsIgnoreCase("sqlChangelog")) {
            uiService.sendMessage(changelogContent.contains("$change") ? "GENERATED CHANGELOG CONTAINS $changeReversed CHANGE" :
                    "FAIL! GENERATED CHANGELOG DOES NOT CONTAIN $changeReversed CHANGE")
            assert changelogContent.contains("$changeReversed")
        }
    }

    static validateSql(String generatedSql, String expectedSql) {
        def message
        if (expectedSql.equalsIgnoreCase(generatedSql)) {
            message = "GENERATED SQL IS CORRECT"
        } else {
            message = "FAIL! Expected sql doesn't match generated sql! Deleting expectedSql file will test that new sql works correctly and " +
                    "will auto-generate a new version if it passes. \nEXPECTED SQL: \n" + expectedSql + " \n GENERATED SQL: \n" + generatedSql
        }
        Scope.getCurrentScope().getUI().sendMessage(message)
        expectedSql.equalsIgnoreCase(generatedSql)
    }

    static validateDiff(String generatedDiff, String expectedDiff) {
        def message
        if (generatedDiff == expectedDiff) {
            message = "GENERATED DIFF IS CORRECT"
        } else {
            message = "FAIL! EXPECTED DIFF DOESN'T MATCH GENERATED DIFF!"
        }
        uiService.sendMessage(message)
        generatedDiff == expectedDiff
    }

    static void saveAsExpectedSql(String generatedSql, TestInput testInput, String commandName) {
        uiService.sendMessage("WARNING! No expectedSql was found! The test will auto-generate new expectedSql file to\n" +
                "src/test/resources/liquibase/harness/compatibility/advanced/expectedSql/${commandName}\n" +
                "folder. Please verify its content and use it as expectedSql test data.")
        File outputFile = "${TestConfig.instance.outputResourcesBase}/liquibase/harness/compatibility/advanced/expectedSql/" +
                "${commandName}/${testInput.databaseName}/${testInput.change}.sql" as File
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
        String version
        String username
        String password
        String url
        String referenceUrl
        String generateChangelogResourcesPath
        String diffChangelogResourcesPath
        String primaryInitSqlPath
        String secondaryInitSqlPath
        String pathToExpectedDiffFile
        String pathToEmptyDiffFile
        String primaryDbSchemaName
        String secondaryDbSchemaName
        String change
        String changeReversed
        String expectedSnapshot
        String expectedGenerateChangelogSql
        String expectedDiffChangelogSql
        Database database
    }
}
