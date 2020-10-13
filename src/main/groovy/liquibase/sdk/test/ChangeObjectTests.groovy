package liquibase.sdk.test

import liquibase.CatalogAndSchema
import liquibase.Liquibase
import liquibase.sdk.test.config.TestConfig
import liquibase.sdk.test.util.FileUtils
import liquibase.sdk.test.util.SnapshotHelpers
import liquibase.sdk.test.util.TestUtils
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
        Assume.assumeTrue(testInput.database != null)

        given:
        Liquibase liquibase = TestUtils.createLiquibase(testInput.pathToChangeLogFile, testInput.database)
        //TODO need to provide ability to override default expected file paths
        String expectedSql = FileUtils.getExpectedSqlFileContent(testInput)
        String expectedSnapshot = FileUtils.getExpectedSnapshotFileContent(testInput)
        List<CatalogAndSchema> catalogAndSchemaList = TestUtils.getCatalogAndSchema(testInput.database, testInput.dbSchema)
        ArrayList<String> expectedSqlList = TestUtils.parseValuesToList(expectedSql, "\n")

        when:
        List<String> generatedSql = TestUtils.toSqlFromLiquibaseChangeSets(liquibase)

        then:
        if(!testInput.pathToChangeLogFile.endsWith(".sql")){
            assert expectedSqlList == generatedSql
        }

        when:
        liquibase.update(testInput.context)

        String jsonSnapshot = SnapshotHelpers.getJsonSnapshot(testInput.database, catalogAndSchemaList)
        liquibase.rollback(liquibase.databaseChangeLog.changeSets.size(), testInput.context)

        then:
        snapshotMatchesSpecifiedStructure(expectedSnapshot, jsonSnapshot)

        where:
        testInput << TestUtils.buildTestInput(config)
    }

    static void snapshotMatchesSpecifiedStructure(String expected, String actual) {
        JSONAssert.assertEquals(expected, actual, new SnapshotHelpers.GeneralSnapshotComparator())
    }
}
