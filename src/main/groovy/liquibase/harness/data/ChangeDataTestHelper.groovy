package liquibase.harness.data

import groovy.transform.ToString
import groovy.transform.builder.Builder
import liquibase.Scope
import liquibase.database.Database
import liquibase.database.DatabaseConnection
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.harness.util.FileUtils

class ChangeDataTestHelper {

    final static List supportedChangeLogFormats = ['xml', 'sql', 'json', 'yml', 'yaml'].asImmutable()
    final static String baseChangelogPath = "liquibase/harness/data/changelogs"

    static List<TestInput> buildTestInput() {
        String commandLineInputFormat = System.getProperty("inputFormat")
        String commandLineChangeData = System.getProperty("changeData")
        List commandLineChangeDataList = Collections.emptyList()
        if(commandLineChangeData){
            commandLineChangeDataList = Arrays.asList(commandLineChangeData.contains(",")
                    ? commandLineChangeData.split(",")
                    : commandLineChangeData)
        }
        if (commandLineInputFormat) {
            if (!supportedChangeLogFormats.contains(commandLineInputFormat)) {
                throw new IllegalArgumentException(commandLineInputFormat + " inputFormat is not supported")
            }
            TestConfig.instance.inputFormat = commandLineInputFormat
        }

        Scope.getCurrentScope().getUI().sendMessage("Only " + TestConfig.instance.inputFormat
                + " input files are taken into account for this test run")

        List<TestInput> inputList = new ArrayList<>()
        DatabaseConnectionUtil databaseConnectionUtil = new DatabaseConnectionUtil()

        for (DatabaseUnderTest databaseUnderTest : databaseConnectionUtil
                .initializeDatabasesConnection(TestConfig.instance.getFilteredDatabasesUnderTest())) {
            def database = databaseUnderTest.database
            for (def changeLogEntry : FileUtils.resolveInputFilePaths(databaseUnderTest,
                    baseChangelogPath,
                    TestConfig.instance.inputFormat).entrySet()) {
                if (!commandLineChangeDataList || commandLineChangeDataList.contains(changeLogEntry.key)) {
                    inputList.add(TestInput.builder()
                            .databaseName(databaseUnderTest.name)
                            .url(databaseUnderTest.url)
                            .dbSchema(databaseUnderTest.dbSchema)
                            .username(databaseUnderTest.username)
                            .password(databaseUnderTest.password)
                            .version(databaseUnderTest.version)
                            .context(TestConfig.instance.context)
                            .changeData(changeLogEntry.key)
                            .pathToChangeLogFile(changeLogEntry.value)
                            .database(database)
                            .build())
                }
            }
        }
        return inputList
    }

    static void saveAsExpectedSql(String generatedSql, TestInput testInput) {
        File outputFile = "${TestConfig.instance.outputResourcesBase}/liquibase/harness/data/expectedSql/" +
                "${testInput.databaseName}/${testInput.changeData}.sql" as File
        outputFile.parentFile.mkdirs()
        try {
            outputFile.write(generatedSql)
        } catch(IOException exception) {
            Scope.getCurrentScope().getUI().sendErrorMessage("Failed to save generated sql file! " + exception.message)
        }
    }

    static boolean shouldOpenNewConnection(DatabaseConnection connection, String... dbNames) {
        return connection.isClosed()||Arrays.stream(dbNames).anyMatch({ dbName -> connection.getDatabaseProductName().toLowerCase().contains(dbName) })
    }

    @Builder
    @ToString(includeNames=true, includeFields=true, includePackage = false, excludes ='database,password')
    static class TestInput {
        String databaseName
        String url
        String dbSchema
        String username
        String password
        String version
        String context
        String changeData
        String pathToChangeLogFile
        Database database
    }
}
