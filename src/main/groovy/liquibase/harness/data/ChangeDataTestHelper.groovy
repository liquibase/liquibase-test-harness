package liquibase.harness.data

import groovy.transform.ToString
import groovy.transform.builder.Builder
import liquibase.Scope
import liquibase.database.Database
import liquibase.database.DatabaseConnection
import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.harness.util.FileUtils

class ChangeDataTestHelper {

    final static List supportedChangeLogFormats = ['xml', 'sql', 'json', 'yml', 'yaml', 'all', 'all-structured'].asImmutable()
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
                for (def changeLogEntry : FileUtils.resolveInputFilePaths(databaseUnderTest,
                        baseChangelogPath,
                        format).entrySet()) {
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

    /**
     * Reopens the database connection if it has been closed.
     * This is needed because Liquibase commands may close connections after execution,
     * but subsequent tests need a live connection to query catalog/schema names.
     */
    static void reopenDatabaseConnectionIfClosed(TestInput testInput) {
        try {
            def connection = testInput.database?.getConnection()
            if (connection instanceof JdbcConnection) {
                // Check if underlying JDBC connection is closed - some drivers throw on isClosed()
                def jdbcConn = (connection as JdbcConnection).getUnderlyingConnection()
                if (jdbcConn == null || jdbcConn.isClosed()) {
                    testInput.database = DatabaseConnectionUtil.initializeDatabase(
                            testInput.url, testInput.username, testInput.password)
                }
            }
        } catch (Exception e) {
            // Connection is in a bad state, reinitialize
            Scope.getCurrentScope().getLog(ChangeDataTestHelper.class).info(
                    "Reopening closed database connection for ${testInput.databaseName}")
            testInput.database = DatabaseConnectionUtil.initializeDatabase(
                    testInput.url, testInput.username, testInput.password)
        }
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
