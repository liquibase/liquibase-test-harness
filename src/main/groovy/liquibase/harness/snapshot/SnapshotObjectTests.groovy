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
    def "Execute inputSql against #input.database.name #input.database.version, generate snapshot #input.snapshotObjectName, compare to expected snapshot"() {
        given: "create arguments map for executing command scope, read expected snapshot from file, apply changes to the database under test"
        def argsMap = new HashMap()
        argsMap.put("url", input.database.url)
        argsMap.put("username", input.database.username)
        argsMap.put("password", input.database.password)
        argsMap.put("snapshotFormat", "json")
        def expectedSnapshot = getResourceContent(input.pathToExpectedSnapshotFile)
        Assume.assumeFalse("Cannot test against offline database", input.database.database.getConnection()
                instanceof OfflineConnection)

        when: "execute inputSql, generate snapshot"
        executeQuery(input.pathToInputSql, input.database.database)
        def generatedSnapshot = executeCommandScope("snapshot", argsMap).toString()

        then: "compare generated to expected snapshot"
        snapshotMatchesSpecifiedStructure(expectedSnapshot, generatedSnapshot)

        cleanup: "execute cleanupSql"
        executeQuery(input.pathToCleanupSql, input.database.database)

        where:
        input << buildTestInput()
    }
}
