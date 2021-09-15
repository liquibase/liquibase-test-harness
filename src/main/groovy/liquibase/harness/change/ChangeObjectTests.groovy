package liquibase.harness.change

import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.TestConfig
import liquibase.harness.util.FileUtils
import org.junit.Assume
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.harness.util.TestUtils.*
import static ChangeObjectTestHelper.*

class ChangeObjectTests extends Specification {

    @Unroll
    def "apply #testInput.changeObject against #testInput.databaseName #testInput.version; verify generated SQL and DB snapshot"() {
        given: "read expected sql and snapshot files, create arguments map for executing command scope"
        String expectedSql = parseQuery(FileUtils.getExpectedSqlFileContent(testInput.changeObject, testInput.databaseName,
                testInput.version, "liquibase/harness/change/expectedSql"))
        String expectedSnapshot = FileUtils.getExpectedJSONFileContent(testInput.changeObject, testInput.databaseName,
                testInput.version, "liquibase/harness/change/expectedSnapshot")
        Map<String, Object> argsMap = new HashMap<>()
        argsMap.put("changeLogFile", testInput.pathToChangeLogFile)
        argsMap.put("url", testInput.url)
        argsMap.put("username", testInput.username)
        argsMap.put("password", testInput.password)
        argsMap.put("snapshotFormat", "JSON")
        argsMap.put("count", getChangeSetsCount(testInput.pathToChangeLogFile))

        and: "skip testcase if it's invalid for this combination of db type and/or version"
        Assume.assumeTrue(expectedSql, expectedSql == null || !expectedSql.toLowerCase().contains("invalid test"))

        and: "fail test if snapshot is not provided"
        assert expectedSnapshot != null: "No expectedSnapshot for ${testInput.changeObject} against " +
                "${testInput.database.shortName} ${testInput.database.databaseMajorVersion}." +
                "${testInput.database.databaseMinorVersion}"

        when: "get sql that is generated for change set"
        def generatedSql = parseQuery(executeCommandScope("updateSql", argsMap).toString())

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
        executeCommandScope("update", argsMap)

        then: "get DB snapshot, rollback changes, check if actual snapshot matches expected snapshot"
        def generatedSnapshot = executeCommandScope("snapshot", argsMap).toString()
        snapshotMatchesSpecifiedStructure(expectedSnapshot, generatedSnapshot)
        executeCommandScope("rollbackCount", argsMap)

        and: "if expected sql is not provided save generated sql as expected sql"
        if (expectedSql == null && !testInput.pathToChangeLogFile.endsWith(".sql")) {
            saveAsExpectedSql(generatedSql, testInput)
        }

        where: "test input in next data table"
        testInput << buildTestInput()
    }
}
