package liquibase.harness.snapshot

import liquibase.database.OfflineConnection
import org.junit.Assume
import spock.lang.Specification
import spock.lang.Unroll

import static SnapshotObjectTestHelper.buildTestInput
import static liquibase.harness.util.SnapshotHelpers.snapshotMatchesSpecifiedStructure
import static liquibase.harness.util.FileUtils.getResourceContent
import static liquibase.harness.util.TestUtils.executeCommandScope
import static liquibase.harness.util.DatabaseConnectionUtil.executeQuery

class SnapshotObjectTests extends Specification {

    @Unroll
    def "Apply #testInput.snapshotObjectName against #testInput.database.name #testInput.database.version"() {
        given: "create arguments map for executing command scope, read expected snapshot from file, " +
                "apply changes to the database under test"
        Map<String, Object> argsMap = new HashMap()
        argsMap.put("url", testInput.database.url)
        argsMap.put("username", testInput.database.username)
        argsMap.put("password", testInput.database.password)
        argsMap.put("snapshotFormat", "json")
        String expectedSnapshot = getResourceContent(testInput.pathToExpectedSnapshotFile)
        Assume.assumeFalse("Cannot test against offline database", testInput.database.database.getConnection()
                instanceof OfflineConnection)
        assert expectedSnapshot != null : "No expectedSnapshot for ${testInput.snapshotObjectName} against " +
                "${testInput.database.name}${testInput.database.version}"

        when: "execute inputSql, generate snapshot"
        executeQuery(testInput.pathToInputSql, testInput.database.database)
        def generatedSnapshot = executeCommandScope("snapshot", argsMap).toString()

        then: "compare generated to expected snapshot"
        snapshotMatchesSpecifiedStructure(expectedSnapshot, generatedSnapshot)

        cleanup: "execute cleanupSql"
        executeQuery(testInput.pathToCleanupSql, testInput.database.database)

        where:
        testInput << buildTestInput()
    }
}
