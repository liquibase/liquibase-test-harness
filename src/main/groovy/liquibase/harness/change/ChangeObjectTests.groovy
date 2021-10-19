package liquibase.harness.change

import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.TestConfig
import org.junit.Assume
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.harness.util.FileUtils.*
import static liquibase.harness.util.SnapshotHelpers.snapshotMatchesSpecifiedStructure
import static liquibase.harness.util.TestUtils.*
import static ChangeObjectTestHelper.*

class ChangeObjectTests extends Specification {

    @Unroll
    def "apply #testInput.changeObject against #testInput.databaseName #testInput.version"() {
        given: "read expected sql and snapshot files, create arguments map for executing command scope"
        String expectedSql = parseQuery(getSqlFileContent(testInput.changeObject, testInput.databaseName, testInput.version,
                "liquibase/harness/change/expectedSql"))
        String expectedSnapshot = getJSONFileContent(testInput.changeObject, testInput.databaseName, testInput.version,
                "liquibase/harness/change/expectedSnapshot")
        Map<String, Object> argsMap = new HashMap()
        argsMap.put("changeLogFile", testInput.pathToChangeLogFile)
        argsMap.put("url", testInput.url)
        argsMap.put("username", testInput.username)
        argsMap.put("password", testInput.password)
        argsMap.put("snapshotFormat", "JSON")
        argsMap.put("count", getChangeSetsCount(testInput.pathToChangeLogFile))

        and: "skip testcase if it's invalid for this combination of db type and/or version"
        boolean runTest = expectedSql == null || !expectedSql.toLowerCase().contains("invalid test")
        Assume.assumeTrue(expectedSql, runTest)

        and: "fail test if snapshot is not provided"
//        when: "if snapshot is not provided"
//        then: "fail test"
        assert expectedSnapshot != null: "No expectedSnapshot for ${testInput.changeObject} against" +
                " ${testInput.database.shortName} ${testInput.database.databaseMajorVersion}." +
                "${testInput.database.databaseMinorVersion}"

        and: "check database under test is online"
//        when: "check database under test is offline"
//        then: "fail test"
        assert testInput.database.getConnection() instanceof JdbcConnection: "Database ${testInput.databaseName}" +
                "${testInput.version} is offline!"

        when: "get sql generated for the change set"
        def generatedSql = parseQuery(executeCommandScope("updateSql", argsMap).toString())

        then: "verify expected sql matches generated sql"
        if (expectedSql != null && !testInput.pathToChangeLogFile.endsWith(".sql")) {
            assert generatedSql == expectedSql: "Expected sql doesn't match generated sql. Deleting expectedSql file" +
                    " will test that new sql works correctly and will auto-generate a new version if it passes"
            if (!TestConfig.instance.revalidateSql) {
                return //sql is right. Nothing more to test
            }
        }

        when: "apply changeSet to DB"
        executeCommandScope("update", argsMap)

        then: "get DB snapshot, check if actual snapshot matches expected snapshot"
        def generatedSnapshot = executeCommandScope("snapshot", argsMap).toString()
        snapshotMatchesSpecifiedStructure(expectedSnapshot, generatedSnapshot)

        and: "if expected sql is not provided save generated sql as expected sql"
        if (expectedSql == null && !testInput.pathToChangeLogFile.endsWith(".sql")) {
            saveAsExpectedSql(generatedSql, testInput)
        }

        cleanup: "rollback changes"
        if (runTest && testInput.database.getConnection() instanceof JdbcConnection) {
            executeCommandScope("rollbackCount", argsMap)
        }

        where: "test input in next data table"
        testInput << buildTestInput()
    }
}
