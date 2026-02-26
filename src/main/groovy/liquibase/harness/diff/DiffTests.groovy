package liquibase.harness.diff

import liquibase.database.jvm.JdbcConnection
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.rollback.RollbackStrategy
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.harness.diff.DiffCommandTestHelper.*
import static liquibase.harness.util.TestUtils.*
import static liquibase.harness.util.FileUtils.*

class DiffTests extends Specification {
    @Shared
    RollbackStrategy strategy
    @Shared
    List<DatabaseUnderTest> databases

    def setupSpec() {
        databases = TestConfig.instance.getFilteredDatabasesUnderTest()
        strategy = chooseRollbackStrategy()
        strategy.prepareForRollback(databases)
    }

    @Unroll
    def "apply diff test against #testInput.referenceDatabase.name #testInput.referenceDatabase.version to targetDatabase #testInput.targetDatabase.name #testInput.targetDatabase.version"() {
        given: "read input data for diff test"
        Map<String, Object> argsMap = new HashMap()
        argsMap.put("url", testInput.targetDatabase.url)
        argsMap.put("username", testInput.targetDatabase.username)
        argsMap.put("password", testInput.targetDatabase.password)
        argsMap.put("referenceUrl", testInput.referenceDatabase.url)
        argsMap.put("referenceUsername", testInput.referenceDatabase.username)
        argsMap.put("referencePassword", testInput.referenceDatabase.password)
        argsMap.put("changelogFile", testInput.pathToChangelogFile)
        if (!"true".equalsIgnoreCase(System.getProperty("useProArtifacts"))) {
            argsMap.put("labelFilter", "!pro")
        }

        and: "check databases are online"
        assert testInput.targetDatabase.database.getConnection() instanceof JdbcConnection: "Target database " +
                "${testInput.targetDatabase.name}${testInput.targetDatabase.version} is offline!"
        assert testInput.referenceDatabase.database.getConnection() instanceof JdbcConnection: "Reference database " +
                "${testInput.referenceDatabase.name}${testInput.referenceDatabase.version} is offline!"

        when: "execute update and diff commands"

        executeCommandScope("update", argsMap)
        argsMap.put("excludeObjects", "(?i)posts, (?i)authors, (?i)databasechangelog, (?i)databasechangeloglock")
        String generatedDiffContent = removeDatabaseInfoFromDiff(executeCommandScope("diff", argsMap).toString())

        then: "validate generated diff"
        String expectedDiffContent = removeDatabaseInfoFromDiff(getResourceContent(testInput.pathToExpectedDiffFile))
        assert expectedDiffContent == generatedDiffContent

        cleanup: "rollback changes"
        strategy.performRollback(argsMap)

        where:
        testInput << buildTestInput()
    }
    def cleanupSpec() {
        strategy.cleanupDatabase(databases)
    }
}
