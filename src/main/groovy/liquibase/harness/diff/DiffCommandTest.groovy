package liquibase.harness.diff

import liquibase.database.jvm.JdbcConnection
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.harness.diff.DiffCommandTestHelper.*
import static liquibase.harness.util.TestUtils.*
import static liquibase.harness.util.JSONUtils.*
import static liquibase.harness.util.FileUtils.*

/**
 * Warning: this test might be destructive, meaning it may change the state of targetDatabase according to referenceDatabase
 */
class DiffCommandTest extends Specification {

    @Unroll
    def "compare referenceDatabase #testInput.referenceDatabase.name #testInput.referenceDatabase.version to targetDatabase #testInput.targetDatabase.name #testInput.targetDatabase.version, verify no significant diffs"() {
        given: "create arguments map for executing command scope, read expected diff from file"
        def argsMap = new HashMap()
        argsMap.put("url", testInput.targetDatabase.url)
        argsMap.put("username", testInput.targetDatabase.username)
        argsMap.put("password", testInput.targetDatabase.password)
        argsMap.put("referenceUrl", testInput.referenceDatabase.url)
        argsMap.put("referenceUsername", testInput.referenceDatabase.username)
        argsMap.put("referencePassword", testInput.referenceDatabase.password)
        argsMap.put("changelogFile", testInput.pathToChangelogFile)
        argsMap.put("format", "json")
        argsMap.put("liquibaseProLicenseKey", getLicenseKey())
        def expectedDiff = getJsonFromResource(getExpectedDiffPath(testInput))
        assert testInput.targetDatabase.database.getConnection() instanceof JdbcConnection : "Target database " +
                "${testInput.targetDatabase.name}${testInput.targetDatabase.version} is offline!"
        assert testInput.referenceDatabase.database.getConnection() instanceof JdbcConnection : "Reference database " +
                "${testInput.referenceDatabase.name}${testInput.referenceDatabase.version} is offline!"

        when: "generate diff changelog, apply changes from generated changelog to target database"
        executeCommandScope("diffChangelog", argsMap)
        executeCommandScope("update", argsMap)

        then: "compare expected diff to generated diff"
        def diffToCompare = createDiffToCompare(executeCommandScope("diff", argsMap))
        compareJSONObjects(expectedDiff, diffToCompare)

        /** Rollback might not take effect in the case generated changelog contains ModifyDataTypeChange
         * or DropDefaultValueChange or others that are not supported by default rollback
         */
        cleanup: "try to rollback changes out from target database, delete generated changelog file"
        argsMap.put("count", getChangeSetsCount(testInput.pathToChangelogFile))
        tryToRollbackDiff(argsMap)
        deleteFile(testInput.pathToChangelogFile)

        where:
        testInput << buildTestInput()
    }
}
