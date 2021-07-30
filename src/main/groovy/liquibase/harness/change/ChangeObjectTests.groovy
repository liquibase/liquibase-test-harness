package liquibase.harness.change

import liquibase.CatalogAndSchema
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.TestConfig
import liquibase.harness.util.FileUtils
import liquibase.harness.util.SnapshotHelpers
import org.junit.Assert
import org.junit.Assume
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.harness.util.TestUtils.*
import static ChangeObjectTestHelper.*

class ChangeObjectTests extends Specification {

    @Unroll
    def "apply #testInput.changeObject against #testInput.databaseName #testInput.version; verify generated SQL and DB snapshot"() {
        given: "create Liquibase connection, read expected sql and snapshot files"
        Liquibase liquibase = createLiquibase(testInput.pathToChangeLogFile, testInput.database)
        String expectedSql = cleanSql(FileUtils.getExpectedSqlFileContent(
                    testInput.changeObject, testInput.databaseName, testInput.version,
                "liquibase/harness/change/expectedSql"))
        String expectedSnapshot = FileUtils.getExpectedJSONFileContent(
                testInput.changeObject, testInput.databaseName, testInput.version,
                "liquibase/harness/change/expectedSnapshot")
        List<CatalogAndSchema> catalogAndSchemaList = getCatalogAndSchema(testInput.database, testInput.dbSchema)

        and: "skip testcase if it's invalid for this combination of db type and/or version"
        Assume.assumeTrue(expectedSql, expectedSql == null || !expectedSql.toLowerCase().contains("invalid test"))

        and: "fail test if snapshot is not provided"
        assert expectedSnapshot != null: "No expectedSnapshot for ${testInput.changeObject} against " +
                "${testInput.database.shortName} ${testInput.database.databaseMajorVersion}.${testInput.database.databaseMinorVersion}"

        when: "get sql that is generated for changeset"
        def generatedSql = cleanSql(toSqlFromLiquibaseChangeSets(liquibase))

        then: "verify expected sql matches generated sql"
        if (expectedSql != null && !testInput.pathToChangeLogFile.endsWith(".sql")) {
            assert generatedSql == expectedSql: "Expected SQL does not match actual sql. " +
                    "Deleting the existing expectedSql file will test that the new SQL works correctly " +
                    "and will auto-generate a new version if it passes"
            if (!TestConfig.instance.revalidateSql) {
                return //sql is right. Nothing more to test
            }
        }

        assert testInput.database.getConnection() instanceof JdbcConnection: "We cannot verify the following SQL works " +
                "because the database is offline:\n${generatedSql}"

        when: "apply changeSet to DB"
        try {
            liquibase.update(testInput.context)
        } catch (Throwable e) {
            println "Error executing sql. If this is expected to be invalid SQL for this database/version, " +
                    "create an 'expectedSql/${testInput.database.shortName}/${testInput.changeObject}.sql' file that starts with " +
                    "'INVALID TEST' and an explanation of why."
            e.printStackTrace()
            Assert.fail e.message
        }

        then: "get DB snapshot, rollback changes, check if actual snapshot matches expected snapshot"
        String jsonSnapshot = SnapshotHelpers.getJsonSnapshot(testInput.database, catalogAndSchemaList)
        liquibase.rollback(liquibase.databaseChangeLog.changeSets.size(), testInput.context)

        snapshotMatchesSpecifiedStructure(expectedSnapshot, jsonSnapshot)

        and: "if expected sql is not provided save generated sql as expected sql"
        if (expectedSql == null && !testInput.pathToChangeLogFile.endsWith(".sql")) {
            saveAsExpectedSql(generatedSql, testInput)
        }

        where: "test input in next data table"
        testInput << buildTestInput()
    }
}
