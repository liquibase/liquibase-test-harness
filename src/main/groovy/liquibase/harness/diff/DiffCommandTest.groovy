package liquibase.harness.diff


import liquibase.Liquibase
import liquibase.Scope
import liquibase.diff.DiffResult
import liquibase.diff.compare.CompareControl
import liquibase.harness.config.TestConfig
import liquibase.resource.FileSystemResourceAccessor
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.harness.diff.DiffCommandTestHelper.*

/**
 * Warning: this is destructive test, meaning it will change state of targetDatabase according to referenceDatabase
 */
class DiffCommandTest extends Specification {

    @Shared CompareControl compareControl

    def setup() {
        compareControl = buildCompareControl()
    }

    @Unroll
    def "compare targetDatabase #testInput.targetDatabase.name #testInput.targetDatabase.version to referenceDatabase #testInput.referenceDatabase.name #testInput.referenceDatabase.version, verify no significant diffs"() {
        given:
        Liquibase liquibase = new Liquibase((String) null, TestConfig.instance.resourceAccessor, testInput.targetDatabase.database)

        DiffResult diffResult = liquibase.diff(testInput.referenceDatabase.database, testInput.targetDatabase.database, compareControl)
        if(diffsAbsent(diffResult)) {
            return
        }
        File tempFile = File.createTempFile("lb-test", ".xml")
        OutputStream outChangeLog = new FileOutputStream(tempFile)
        String changeLogString = toChangeLog(diffResult)
        outChangeLog.write(changeLogString.getBytes("UTF-8"))
        outChangeLog.close()

        Scope.getCurrentScope().getLog(DiffCommandTest.class).info("Changelog:\n" + changeLogString)

        when:

        liquibase = new Liquibase(tempFile.toString(), new FileSystemResourceAccessor(File.listRoots()), testInput.targetDatabase.database)
        liquibase.update(testInput.context)

        DiffResult newDiffResult =  liquibase.diff(testInput.referenceDatabase.database, testInput.targetDatabase.database, compareControl)
        if(diffsAbsent(newDiffResult)) {
            return
        }

        //TODO think about rollback as after test execution database change it's state. default rollback doesn't work as
        // generated changelog can contain ModifyDataTypeChange DropDefaultValueChange or others that don't have rollback
        //liquibase.rollback(liquibase.databaseChangeLog.changeSets.size(), testInput.context);

        removeExpectedDiffs(testInput.expectedDiffs, newDiffResult)

        then:
        newDiffResult.getMissingObjects().size() == 0
        newDiffResult.getUnexpectedObjects().size() == 0
        newDiffResult.getChangedObjects().size() == 0

        where:
        testInput << buildTestInput()
    }
}
