package liquibase.harness.snapshot

import liquibase.database.OfflineConnection
import org.json.JSONObject
import org.junit.Assume
import spock.lang.Specification
import spock.lang.Unroll

import static SnapshotObjectTestHelper.*
import static liquibase.harness.util.JSONUtils.*
import static liquibase.harness.util.TestUtils.*

class SnapshotObjectTests extends Specification {

    @Unroll
    def "Update #input.database.name, generate snapshot #input.snapshotObjectName, compare expected to generated snapshot"() {
        given: "create arguments map for executing command scope, read expected snapshot from file, apply changes to the database under test"
        def argsMap = new HashMap()
        argsMap.put("url", input.database.url)
        argsMap.put("username", input.database.username)
        argsMap.put("password", input.database.password)
        argsMap.put("format", "json")
        argsMap.put("changeLogFile", input.pathToChangelogFile)
        argsMap.put("count", getChangeSetsCount(input.pathToChangelogFile))
        def expectedSnapshot = new JSONObject(getJsonFromResource(input.pathToExpectedSnapshotFile))
        Assume.assumeFalse("Cannot test against offline database", input.database.database.getConnection()
                instanceof OfflineConnection)
        executeCommandScope("update", argsMap)

        when: "generate snapshot"
        def string = executeCommandScope("snapshot", argsMap).toString()
        def generatedSnapshot = new JSONObject(string)

        then: "compare generated to expected snapshot"
        compareJSONObjects(expectedSnapshot, generatedSnapshot)

        cleanup: "rollback changes from database under test"
        executeCommandScope("rollbackCount", argsMap)

        where:
        input << buildTestInput()
    }
}
