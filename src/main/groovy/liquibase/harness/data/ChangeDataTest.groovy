package liquibase.harness.data

import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.TestConfig
import org.json.JSONObject
import org.junit.Assert
import org.junit.Assume
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.harness.util.JSONUtils.*
import static liquibase.harness.util.FileUtils.*
import static liquibase.harness.util.TestUtils.*
import static ChangeDataTestHelper.*

class ChangeDataTest extends Specification {

    @Unroll
    def "apply #testInput.changeData against #testInput.databaseName, #testInput.version; verify generated query, checking query and obtained result set"() {

        Liquibase liquibase = createLiquibase(testInput.pathToChangeLogFile, testInput.database)
        given: "create Liquibase connection, read expected sql and obtain result set"
        String expectedSql = cleanSql(getExpectedSqlFileContent(testInput.changeData,
                testInput.databaseName, testInput.version, "liquibase/harness/data/expectedSql"))
        String checkingSql = cleanSql(getExpectedSqlFileContent(testInput.changeData,
                testInput.databaseName, testInput.version, "liquibase/harness/data/checkingSql"))
        String expectedResultSet = getExpectedJSONFileContent(testInput.changeData, testInput.databaseName,
                testInput.version, "liquibase/harness/data/expectedResultSet")

        and: "skip testcase if it's invalid for this combination of db type and/or version"
        Assume.assumeTrue(expectedSql, expectedSql == null || !expectedSql.toLowerCase().contains("invalid test"))

        and: "fail test if expectedResultSet is not provided"
        assert expectedResultSet != null: "No expectedResultSet for ${testInput.changeData} against " +
                "${testInput.database.shortName} " +
                "${testInput.database.databaseMajorVersion}.${testInput.database.databaseMinorVersion}"

        and: "fail test if checkingSql is not provided"
        assert checkingSql != null: "No checkingSql for ${testInput.changeData} against " +
                "${testInput.database.shortName} " +
                "${testInput.database.databaseMajorVersion}.${testInput.database.databaseMinorVersion}"

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

        def connection = testInput.database.getConnection()

        assert connection instanceof JdbcConnection: "We cannot verify the following SQL works " +
                "because the database is offline:\n${generatedSql}"

        when: "apply changeSet to DB,"
        try {
            liquibase.update(testInput.context)
        } catch (Throwable throwable) {
            println "Error executing sql. If this is expected to be invalid SQL for this database/version, " +
                    "create an 'expectedSql/${testInput.database.shortName}/${testInput.changeData}.sql' file that starts with " +
                    "'INVALID TEST' and an explanation of why."
            throwable.printStackTrace()
            Assert.fail throwable.message
        }

        then: "obtain resultSet form the statement, compare expected resultSet to generated resultSet, apply rollback"
        try {
            def statement = connection.createStatement()
            def resultSet = statement.executeQuery(checkingSql)
            def generatedResultSetArray = mapResultSetToJSONArray(resultSet)
            def expectedResultSeJSON = new JSONObject(expectedResultSet)
            def expectedResultSetArray = expectedResultSeJSON.getJSONArray(testInput.getChangeData())
            assert compareJSONArrays(generatedResultSetArray, expectedResultSetArray)
        } catch (Throwable throwable) {
            println "Error executing checking sql. "
            throwable.printStackTrace()
            Assert.fail throwable.message
        }

        liquibase.rollback(liquibase.databaseChangeLog.changeSets.size(), testInput.context)

        and: "if expected sql is not provided save generated sql as expected sql"
        if (expectedSql == null && !testInput.pathToChangeLogFile.endsWith(".sql")) {
            saveAsExpectedSql(generatedSql, testInput)
        }

        where: "test input in next data table"
        testInput << buildTestInput()
    }
}
