package liquibase.harness.change

import liquibase.Scope
import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.TestConfig
import org.junit.Assert
import org.junit.Assume
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.SQLException

import static liquibase.harness.util.FileUtils.*
import static liquibase.harness.util.SnapshotHelpers.snapshotMatchesSpecifiedStructure
import static liquibase.harness.util.TestUtils.*
import static ChangeObjectTestHelper.*

class ChangeObjectTests extends Specification {
    @Shared Map<String, Object> argsMap
    @Shared List<TestInput> testInputs
    def setupSpec() {
        testInputs = buildTestInput()
        argsMap = new HashMap()
        argsMap.put("url",  testInputs.get(0).url)
        argsMap.put("username",  testInputs.get(0).username)
        argsMap.put("password",  testInputs.get(0).password)
        argsMap.put("snapshotFormat", "JSON")
        argsMap.put("tag","test-harness-tag")
        executeCommandScope("tag", argsMap)
    }
    @Unroll
    def "apply #testInput.changeObject against #testInput.databaseName #testInput.version"() {
        given: "read expected sql and snapshot files, create arguments map for executing command scope"
        String expectedSql = parseQuery(getSqlFileContent(testInput.changeObject, testInput.databaseName, testInput.version,
                "liquibase/harness/change/expectedSql"))
        String expectedSnapshot = getJSONFileContent(testInput.changeObject, testInput.databaseName, testInput.version,
                "liquibase/harness/change/expectedSnapshot")
        boolean shouldRunChangeSet
        argsMap.put("changeLogFile", testInput.pathToChangeLogFile)

        and: "ignore testcase if it's invalid for this combination of db type and/or version"
        shouldRunChangeSet = !expectedSql?.toLowerCase()?.contains("invalid test")
        Assume.assumeTrue(expectedSql, shouldRunChangeSet)

        and: "fail test if snapshot is not provided"
        shouldRunChangeSet = expectedSnapshot != null
        assert shouldRunChangeSet: "No expectedSnapshot for ${testInput.changeObject} against" +
                " ${testInput.database.shortName} ${testInput.database.databaseMajorVersion}." +
                "${testInput.database.databaseMinorVersion}"

        and: "check database under test is online"
        shouldRunChangeSet = testInput.database.getConnection() instanceof JdbcConnection
        assert shouldRunChangeSet: "Database ${testInput.databaseName} ${testInput.version} is offline!"

        when: "get sql generated for the change set"
        def generatedSql = parseQuery(executeCommandScope("updateSql", argsMap).toString())

        then: "verify expected sql matches generated sql"
        if (expectedSql != null && !testInput.pathToChangeLogFile.endsWith(".sql")) {
            //TODO form nice error message to see expected and actual SQL in logs and remove 2 times in comparison for
            // boolean flag and for assert
            shouldRunChangeSet = generatedSql == expectedSql
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
        if (expectedSql == null && generatedSql != null && !testInput.pathToChangeLogFile.endsWith(".sql") && !generatedSql.isEmpty()) {
            saveAsExpectedSql(generatedSql, testInput)
        }

        cleanup: "rollback changes if we ran changeSet"
        if (shouldRunChangeSet) {
            executeCommandScope("rollback", argsMap)
//            executeCommandScope("rollbackToDate", argsMap)
        }

        where: "test input in next data table"
        testInput << testInputs
    }

    def cleanupSpec() {
        def connection = testInputs.get(0).database.getConnection()
        try {
            ((JdbcConnection) connection).createStatement().executeUpdate("delete from DATABASECHANGELOG where TAG='${argsMap.get("tag")}'")
        } catch (SQLException sqlException) {
            // Assume test object was not created after 'update' command execution and test failed.
            Scope.getCurrentScope().getUI().sendMessage("Couldn't delete ${argsMap.get("tag")} tag from tracking table " +
                    sqlException.printStackTrace())
            Assert.fail sqlException.message
        } finally {
            connection.commit()
        }
    }
}
