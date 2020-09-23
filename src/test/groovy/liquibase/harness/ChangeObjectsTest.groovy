package liquibase.harness

import liquibase.CatalogAndSchema
import liquibase.Liquibase
import liquibase.database.Database
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.harness.util.FileUtils
import liquibase.harness.util.SnapshotHelpers
import liquibase.harness.util.TestUtils
import org.skyscreamer.jsonassert.JSONAssert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll


class ChangeObjectsTest extends Specification {
    @Shared Logger logger = LoggerFactory.getLogger(getClass())
    @Shared
    TestConfig config

    def setupSpec() {
        config = FileUtils.readYamlConfig("testConfig.yml")
        TestUtils.validateAndSetPropertiesFromCommandLine(config)
    }

    @Unroll
    def "apply #testInput.changeObject for #testInput.databaseName #testInput.version; verify generated SQL and DB snapshot"() {
        given:
        Database database = DatabaseConnectionUtil.initializeDatabase(testInput)
        Liquibase liquibase = TestUtils.createLiquibase(testInput.pathToChangeLogFile, database)
        //TODO need to provide ability to override default expected file paths
        String expectedSql = FileUtils.getExpectedSqlFileContent(testInput)
        String expectedSnapshot = FileUtils.getExpectedSnapshotFileContent(testInput)
        List<CatalogAndSchema> catalogAndSchemaList = TestUtils.getCatalogAndSchema(database, testInput.dbSchema)
        ArrayList<String> expectedSqlList = TestUtils.parseValuesToList(expectedSql, "\n")

        when:
        List<String> generatedSql = TestUtils.toSqlFromLiquibaseChangeSets(liquibase)

        then:
        if(!testInput.pathToChangeLogFile.endsWith(".sql")){
            assert expectedSqlList == generatedSql
        }

        when:
        liquibase.update(testInput.context)

        String jsonSnapshot = SnapshotHelpers.getJsonSnapshot(database, catalogAndSchemaList)
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