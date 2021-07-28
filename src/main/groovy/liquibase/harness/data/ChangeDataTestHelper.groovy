package liquibase.harness.data

import groovy.transform.ToString
import groovy.transform.builder.Builder
import liquibase.database.Database
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.harness.util.TestUtils

import java.util.logging.Logger

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

        Logger.getLogger(this.class.name).warning("Only " + TestConfig.instance.inputFormat
                + " input files are taken into account for this test run")

        List<TestInput> inputList = new ArrayList<>()
        DatabaseConnectionUtil databaseConnectionUtil = new DatabaseConnectionUtil()

        for (DatabaseUnderTest databaseUnderTest : databaseConnectionUtil
                .initializeDatabasesConnection(TestConfig.instance.databasesUnderTest)) {
            def database = databaseUnderTest.database
            for (def changeLogEntry : TestUtils.resolveInputFilePaths(databaseUnderTest,
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
