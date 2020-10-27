package liquibase.sdk.test.util

import groovy.transform.builder.Builder
import liquibase.database.Database
import liquibase.sdk.test.config.DatabaseUnderTest
import liquibase.sdk.test.config.TestConfig
import liquibase.util.StringUtil
import org.skyscreamer.jsonassert.JSONAssert

import java.util.logging.Logger

class ChangeObjectTestHelper {

    final static List supportedChangeLogFormats = ['xml', 'sql', 'json', 'yml', 'yaml'].asImmutable()

    static List<TestInput> buildTestInput() {
        String inputFormat = System.getProperty("inputFormat")
        String changeObjects = System.getProperty("changeObjects")
        if (inputFormat && (!supportedChangeLogFormats.contains(inputFormat))) {
            throw new IllegalArgumentException(inputFormat + " inputFormat is not supported")
        }

        Logger.getLogger(this.class.name).warning("Only " + inputFormat + " input files are taken into account for this test run")

        List<TestInput> inputList = new ArrayList<>()
        for (DatabaseUnderTest databaseUnderTest : TestConfig.instance.databasesUnderTest) {
            def database = databaseUnderTest.database
            for (def changeLogEntry : TestUtils.getChangeLogPaths(database).entrySet()) {
                if (!changeObjects || (changeObjects && changeObjects.contains(changeLogEntry.key))) {
                    if (!inputFormat || inputFormat && changeLogEntry.value.endsWith(inputFormat)) {
                        inputList.add(TestInput.builder()
                                .databaseName(databaseUnderTest.name)
                                .url(databaseUnderTest.url)
                                .dbSchema(databaseUnderTest.dbSchema)
                                .username(databaseUnderTest.username)
                                .password(databaseUnderTest.password)
                                .version(database.getDatabaseProductVersion())
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
        File outputFile = "${TestConfig.instance.outputResourcesBase}/liquibase/sdk/test/expectedSql/" +
                "${testInput.database.shortName}/${testInput.changeObject}.sql" as File
        outputFile.parentFile.mkdirs()
        outputFile.write(generatedSql)
    }


    @Builder
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
