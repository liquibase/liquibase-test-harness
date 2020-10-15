package liquibase.sdk.test

import liquibase.CatalogAndSchema
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.sdk.test.config.TestConfig
import liquibase.sdk.test.config.TestInput
import liquibase.sdk.test.util.FileUtils
import liquibase.sdk.test.util.SnapshotHelpers
import liquibase.sdk.test.util.TestUtils
import liquibase.util.StringUtil
import org.junit.Assume
import org.skyscreamer.jsonassert.JSONAssert
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class ChangeObjectTests extends Specification {

    @Shared
    TestConfig config

    def setupSpec() {
        config = FileUtils.readYamlConfig("/liquibase.sdk.test.yml")
        TestUtils.validateAndSetPropertiesFromCommandLine(config)
    }

    @Unroll
    def "apply #testInput.changeObject for #testInput.databaseName #testInput.version; verify generated SQL and DB snapshot"() {
        given:
        Liquibase liquibase = TestUtils.createLiquibase(testInput.pathToChangeLogFile, testInput.database)

        String expectedSql = cleanSql(FileUtils.getExpectedSqlFileContent(testInput))
        String expectedSnapshot = FileUtils.getExpectedSnapshotFileContent(testInput)
        List<CatalogAndSchema> catalogAndSchemaList = TestUtils.getCatalogAndSchema(testInput.database, testInput.dbSchema)

        when:
        def generatedSql = cleanSql(TestUtils.toSqlFromLiquibaseChangeSets(liquibase))

        then:
        assert expectedSnapshot != null : "No expectedSnapshot for ${testInput.changeObject} on ${testInput.database.shortName} ${testInput.database.databaseMajorVersion}.${testInput.database.databaseMinorVersion}"

        if (expectedSql != null && !testInput.pathToChangeLogFile.endsWith(".sql")) {
            assert generatedSql == expectedSql : "Expected SQL does not match actual sql. Deleting the existing expectedSql file will test that the new SQL works correctly and will auto-generate a new version if it passes"
            if (!TestUtils.revalidateSql) {
                return //sql is right. Nothing more to test
            }
        }

        assert testInput.database.getConnection() instanceof JdbcConnection : "SQL changed, but we cannot verify if it still works because the database is offline"

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
        testInput << TestUtils.buildTestInput(config)
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
                .replaceAll(/^\s+/, "") //remove beginning whitepace per line
                .replaceAll(/\s+$/, "")) //remove trailing whitepace per line
    }

    static void snapshotMatchesSpecifiedStructure(String expected, String actual) {
        JSONAssert.assertEquals(expected, actual, new SnapshotHelpers.GeneralSnapshotComparator())
    }

    void saveAsExpectedSql(String generatedSql, TestInput testInput) {
        File outputFile = "src/test/resources/liquibase/sdk/test/expectedSql/${testInput.database.shortName}/${testInput.changeObject}.sql" as File
        outputFile.parentFile.mkdirs()

        outputFile.write(generatedSql)
    }
}
