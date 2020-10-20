package liquibase.sdk.test

import groovy.transform.builder.Builder
import liquibase.CatalogAndSchema
import liquibase.Liquibase
import liquibase.database.Database
import liquibase.database.jvm.JdbcConnection
import liquibase.sdk.test.config.DatabaseUnderTest
import liquibase.sdk.test.config.TestConfig
import liquibase.sdk.test.util.FileUtils
import liquibase.sdk.test.util.SnapshotHelpers
import liquibase.sdk.test.util.TestUtils
import liquibase.util.StringUtil
import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Specification
import spock.lang.Unroll

import java.util.logging.Logger

class ChangeObjectTests extends Specification {

    final static List supportedChangeLogFormats = ['xml', 'sql', 'json', 'yml', 'yaml'].asImmutable()

    @Unroll
    def "apply #testInput.changeObject against #testInput.databaseName; verify generated SQL and DB snapshot"() {
        given:
        Liquibase liquibase = TestUtils.createLiquibase(testInput.pathToChangeLogFile, testInput.database)

        String expectedSql = cleanSql(FileUtils.getExpectedSqlFileContent(testInput.changeObject, testInput.database.shortName, testInput.database.databaseMajorVersion, testInput.database.databaseMinorVersion))
        String expectedSnapshot = FileUtils.getExpectedSnapshotFileContent(testInput.changeObject, testInput.database.shortName, testInput.database.databaseMajorVersion, testInput.database.databaseMinorVersion)
        List<CatalogAndSchema> catalogAndSchemaList = TestUtils.getCatalogAndSchema(testInput.database, testInput.dbSchema)

        when:
        def generatedSql = cleanSql(TestUtils.toSqlFromLiquibaseChangeSets(liquibase))

        then:
        assert expectedSnapshot != null: "No expectedSnapshot for ${testInput.changeObject} against ${testInput.database.shortName} ${testInput.database.databaseMajorVersion}.${testInput.database.databaseMinorVersion}"

        if (expectedSql != null && !testInput.pathToChangeLogFile.endsWith(".sql")) {
            assert generatedSql == expectedSql: "Expected SQL does not match actual sql. Deleting the existing expectedSql file will test that the new SQL works correctly and will auto-generate a new version if it passes"
            if (!TestConfig.instance.revalidateSql) {
                return //sql is right. Nothing more to test
            }
        }

        assert testInput.database.getConnection() instanceof JdbcConnection: "We cannot verify the following SQL works works because the database is offline:\n${generatedSql}"

        when:
        liquibase.update(testInput.context)

        String jsonSnapshot = SnapshotHelpers.getJsonSnapshot(testInput.database, catalogAndSchemaList)
        liquibase.rollback(liquibase.databaseChangeLog.changeSets.size(), testInput.context)

        then:
        snapshotMatchesSpecifiedStructure(expectedSnapshot, jsonSnapshot)

        if (expectedSql == null && !testInput.pathToChangeLogFile.endsWith(".sql")) {
            //save generated sql as expected sql for future runs
            saveAsExpectedSql(generatedSql, testInput)
        }

        where:
        testInput << buildTestInput()
    }

    /**
     * Standardizes sql content. Removes line ending differences, and unnessisary leading/trailing whitespace
     * @param sql
     * @return
     */
    protected String cleanSql(String sql) {
        if (sql == null) {
            return null
        }
        return StringUtil.trimToNull(sql.replace("\r", "")
                .replaceAll(/(?m)^--.*/, "") //remove comments
                .replaceAll(/(?m)^\s+/, "") //remove beginning whitepace per line
                .replaceAll(/(?m)\s+$/, "") //remove trailing whitespace per line
        ) //remove trailing whitepace per line
    }

    static void snapshotMatchesSpecifiedStructure(String expected, String actual) {
        JSONAssert.assertEquals(expected, actual, new SnapshotHelpers.GeneralSnapshotComparator())
    }

    void saveAsExpectedSql(String generatedSql, TestInput testInput) {
        File outputFile = "${outputResourcesBase}/liquibase/sdk/test/expectedSql/${testInput.database.shortName}/${testInput.changeObject}.sql" as File
        outputFile.parentFile.mkdirs()

        outputFile.write(generatedSql)
    }

    List<TestInput> buildTestInput() {
        //TODO: Handle changeObject selection
        String inputFormat = System.getProperty("inputFormat")
        String changeObjects = System.getProperty("changeObjects")
        if (inputFormat && (!supportedChangeLogFormats.contains(inputFormat))) {
            throw new IllegalArgumentException(inputFormat + " inputFormat is not supported")
        }

        Logger.getLogger(this.class.name).warning("Only " + inputFormat + " input files are taken into account for this test run")

//        if (changeObjects) {
//            testConfig.defaultChangeObjects = Arrays.asList(changeObjects.split(","))
//            //in case user provided changeObjects in cmd run only them regardless of config file
//            for (def db : testConfig.databasesUnderTest) {
//                db.databaseSpecificChangeObjects = null
//            }
//            log.info("running for next changeObjects : " + testConfig.defaultChangeObjects)
//        }


        List<TestInput> inputList = new ArrayList<>()
        for (DatabaseUnderTest databaseUnderTest : TestConfig.instance.databasesUnderTest) {
            def database = databaseUnderTest.database
            for (def changeLogEntry : TestUtils.getChangeLogPaths(database).entrySet()) {

                def testInput = TestInput.builder()
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
                        .build()

                inputList.add(testInput)
            }
        }
        return inputList
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
