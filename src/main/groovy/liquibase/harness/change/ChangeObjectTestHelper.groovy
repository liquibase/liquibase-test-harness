package liquibase.harness.change

import groovy.transform.ToString
import groovy.transform.builder.Builder
import liquibase.Scope
import liquibase.database.Database
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.harness.util.FileUtils

class ChangeObjectTestHelper {

    final static List supportedChangeLogFormats = ['xml', 'sql', 'json', 'yml', 'yaml', 'all', 'all-structured'].asImmutable()
    final static String baseChangelogPath = "liquibase/harness/change/changelogs"

    static List<TestInput> buildTestInput() {
        String commandLineInputFormat = System.getProperty("inputFormat")
        String commandLineChangeObjects = System.getProperty("changeObjects")
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

        // Determine which formats to run
        List<String> formatsToRun = []
        if (TestConfig.instance.inputFormat == 'all') {
            formatsToRun = ['xml', 'sql', 'json', 'yml', 'yaml']
            Scope.getCurrentScope().getUI().sendMessage(
                    "All input formats (xml, sql, json, yml, yaml) are taken into account for this test run")
        } else if (TestConfig.instance.inputFormat == 'all-structured') {
            formatsToRun = ['xml', 'json', 'yml', 'yaml']
            Scope.getCurrentScope().getUI().sendMessage(
                    "All structured input formats (xml, json, yml, yaml) are taken into account for this test run")
        } else {
            formatsToRun = [TestConfig.instance.inputFormat]
            Scope.getCurrentScope().getUI().sendMessage("Only " + TestConfig.instance.inputFormat
                    + " input files are taken into account for this test run")
        }

        List<TestInput> inputList = new ArrayList<>()
        DatabaseConnectionUtil databaseConnectionUtil = new DatabaseConnectionUtil()

        for (DatabaseUnderTest databaseUnderTest : databaseConnectionUtil
                .initializeDatabasesConnection(TestConfig.instance.getFilteredDatabasesUnderTest())) {
            def database = databaseUnderTest.database

            // Loop through each format to run
            for (String format : formatsToRun) {
                for (def changeLogEntry : FileUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath,
                        format).entrySet()) {
                    if (!commandLineChangeObjectList || commandLineChangeObjectList.contains(changeLogEntry.key)) {
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
