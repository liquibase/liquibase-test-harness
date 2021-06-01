package liquibase.harness.change

import groovy.transform.ToString
import groovy.transform.builder.Builder
import liquibase.database.Database
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.harness.util.SnapshotHelpers
import liquibase.harness.util.TestUtils
import liquibase.util.StringUtil
import org.skyscreamer.jsonassert.JSONAssert

import java.util.logging.Logger

class ChangeObjectTestHelper {

    final static List supportedChangeLogFormats = ['xml', 'sql', 'json', 'yml', 'yaml'].asImmutable()

    final static String baseChangelogPath = "liquibase/harness/change/changelogs"

    static List<TestInput> buildTestInput() {
        String commandLineInputFormat = System.getProperty("inputFormat")

        String commandLineChangeObjects = System.getProperty("changeObjects")
        List commandLineChangeObjectList = Collections.emptyList()
        if(commandLineChangeObjects){
            commandLineChangeObjectList = Arrays.asList(commandLineChangeObjects.contains(",") ? commandLineChangeObjects.split(",") : commandLineChangeObjects)
        }
        if (commandLineInputFormat) {
            if (!supportedChangeLogFormats.contains(commandLineInputFormat)) {
                throw new IllegalArgumentException(commandLineInputFormat + " inputFormat is not supported")
            }
            TestConfig.instance.inputFormat = commandLineInputFormat
        }

        Logger.getLogger(this.class.name).warning("Only " +  TestConfig.instance.inputFormat + " input files are taken into account for this test run")

        List<TestInput> inputList = new ArrayList<>()
        DatabaseConnectionUtil databaseConnectionUtil = new DatabaseConnectionUtil()

        for (DatabaseUnderTest databaseUnderTest : databaseConnectionUtil.initializeDatabasesConnection(TestConfig.instance.databasesUnderTest)) {
            def database = databaseUnderTest.database
            for (def changeLogEntry : TestUtils.resolveInputFilePaths(databaseUnderTest, baseChangelogPath,  TestConfig.instance.inputFormat).entrySet()) {
                if (!commandLineChangeObjectList || commandLineChangeObjectList.contains(changeLogEntry.key)) {2

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

    /**
     * Standardizes sql content. Removes line ending differences, and unnessisary leading/trailing whitespace
     * @param sql
     * @return
     */
    static String cleanSql(String sql) {
        if (sql == null) {
            return null
        }
        return StringUtil.trimToNull(sql.replace("\r", "")
                .replaceAll(/(?m)^--.*/, "") //remove comments
                .replaceAll(/(?m)^\s+/, "") //remove beginning whitepace per line
                .replaceAll(/(?m)\s+$/, "") //remove trailing whitespace per line
        ) //remove trailing whitespace per line
    }

    static void snapshotMatchesSpecifiedStructure(String expected, String actual) {
        JSONAssert.assertEquals(expected, actual, new SnapshotHelpers.GeneralSnapshotComparator())
    }

    static void saveAsExpectedSql(String generatedSql, TestInput testInput) {
        File outputFile = "${TestConfig.instance.outputResourcesBase}/liquibase/harness/change/expectedSql/" +
                "${testInput.databaseName}/${testInput.changeObject}.sql" as File
        outputFile.parentFile.mkdirs()
        outputFile.write(generatedSql)
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
        String changeObject
        String pathToChangeLogFile
        Database database
    }
}
