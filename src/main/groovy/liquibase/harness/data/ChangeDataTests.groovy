package liquibase.harness.data

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

class ChangeDataTests extends Specification {

    @Unroll
    def "apply #testInput.changeData against #testInput.databaseName, #testInput.version; verify generated query, checking query and obtained result set"() {

        given: "read expected sql, checking sql and expected result set, create arguments map for executing command scope"
        String expectedSql = parseQuery(getExpectedSqlFileContent(testInput.changeData,
                testInput.databaseName, testInput.version, "liquibase/harness/data/expectedSql"))
        String checkingSql = parseQuery(getExpectedSqlFileContent(testInput.changeData,
                testInput.databaseName, testInput.version, "liquibase/harness/data/checkingSql"))
        String expectedResultSet = getExpectedJSONFileContent(testInput.changeData, testInput.databaseName,
                testInput.version, "liquibase/harness/data/expectedResultSet")
        Map<String, Object> argsMap = new HashMap<>()
        argsMap.put("url", testInput.url)
        argsMap.put("username", testInput.username)
        argsMap.put("password", testInput.password)
        argsMap.put("changeLogFile", testInput.pathToChangeLogFile)
        argsMap.put("count", getChangeSetsCount(testInput.pathToChangeLogFile))

        and: "skip testcase if it's invalid for this combination of db type and/or version"
        Assume.assumeTrue(expectedSql, expectedSql == null || !expectedSql.toLowerCase().contains("invalid test"))

        and: "fail test if expectedResultSet is not provided"
        assert expectedResultSet != null: "No expectedResultSet for ${testInput.changeData} against " +
                "${testInput.database.shortName} ${testInput.database.databaseMajorVersion}." +
                "${testInput.database.databaseMinorVersion}"

        and: "fail test if checkingSql is not provided"
        assert checkingSql != null: "No checkingSql for ${testInput.changeData} against " +
                "${testInput.database.shortName} ${testInput.database.databaseMajorVersion}." +
                "${testInput.database.databaseMinorVersion}"

        when: "get sql that is generated for change set"
        def generatedSql = parseQuery(executeCommandScope("updateSql", argsMap).toString())

        then: "verify expected sql matches generated sql"
        if (expectedSql != null && !testInput.pathToChangeLogFile.endsWith(".sql")) {
            assert generatedSql == expectedSql: "Expected SQL does not match generated SQL. " +
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
        executeCommandScope("update", argsMap)

        then: "obtain resultSet form the statement, compare expected resultSet to generated resultSet, apply rollback"
        try {
            def generatedResultSetArray = mapResultSetToJSONArray(connection.createStatement().executeQuery(checkingSql))
            def expectedResultSetJSON = new JSONObject(expectedResultSet)
            def expectedResultSetArray = expectedResultSetJSON.getJSONArray(testInput.getChangeData())
            assert compareJSONArrays(generatedResultSetArray, expectedResultSetArray)
        } catch (Throwable throwable) {
            println "Error executing checking SQL."
            throwable.printStackTrace()
            Assert.fail throwable.message
        }
        executeCommandScope("rollbackCount", argsMap)

        and: "if expected sql is not provided save generated SQL as expected SQL"
        if (expectedSql == null && !testInput.pathToChangeLogFile.endsWith(".sql")) {
            saveAsExpectedSql(generatedSql, testInput)
        }

        where: "test input in next data table"
        testInput << buildTestInput()
    }
}

