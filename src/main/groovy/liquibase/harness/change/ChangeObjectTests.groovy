package liquibase.harness.change

import liquibase.CatalogAndSchema
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.TestConfig
import liquibase.harness.util.FileUtils
import liquibase.harness.util.SnapshotHelpers
import liquibase.harness.util.TestUtils
import org.junit.Assert
import org.junit.Assume
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.harness.util.ChangeObjectTestHelper.*

class ChangeObjectTests extends Specification {

    @Unroll
    def "apply #testInput.changeObject against #testInput.databaseName #testInput.version; verify generated SQL and DB snapshot"() {
        given:
        Liquibase liquibase = TestUtils.createLiquibase(testInput.pathToChangeLogFile, testInput.database)

        String expectedSql = cleanSql(FileUtils.getExpectedSqlFileContent(
                testInput.changeObject, testInput.databaseName, testInput.version))
        String expectedSnapshot = FileUtils.getExpectedSnapshotFileContent(
                testInput.changeObject, testInput.databaseName, testInput.version)
        List<CatalogAndSchema> catalogAndSchemaList = TestUtils.getCatalogAndSchema(testInput.database, testInput.dbSchema)

        Assume.assumeTrue(expectedSql, expectedSql == null || !expectedSql.toLowerCase().contains("invalid test"))

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
        try {
            liquibase.update(testInput.context)
        } catch (Throwable e) {
            println "Error executing sql. If this is expected to be invalid SQL for this database/version, create an 'expectedSql/${testInput.database.shortName}/${testInput.changeObject}.sql' file that starts with 'INVALID TEST' and an explanation of why."
            e.printStackTrace()
            Assert.fail e.message
        }

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

}
