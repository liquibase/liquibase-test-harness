package liquibase.harness.diff


import liquibase.Liquibase
import liquibase.Scope
import liquibase.diff.DiffResult
import liquibase.diff.compare.CompareControl
import liquibase.harness.config.TestConfig
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.FileSystemResourceAccessor
import liquibase.structure.DatabaseObject
import liquibase.structure.core.Column
import liquibase.structure.core.ForeignKey
import liquibase.structure.core.Index
import liquibase.structure.core.PrimaryKey
import liquibase.structure.core.Sequence
import liquibase.structure.core.Table
import liquibase.structure.core.UniqueConstraint
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static junit.framework.TestCase.assertEquals
import static junit.framework.TestCase.assertEquals
import static liquibase.harness.diff.DiffCommandTestHelper.*


class DiffCommandTest extends Specification {

    @Shared CompareControl compareControl

    def setup() {
        Set<Class<? extends DatabaseObject>> typesToInclude = new HashSet<Class<? extends DatabaseObject>>()
        typesToInclude.add(Table.class)
        typesToInclude.add(Column.class)
        typesToInclude.add(PrimaryKey.class)
        typesToInclude.add(ForeignKey.class)
        typesToInclude.add(UniqueConstraint.class)
        typesToInclude.add(Sequence.class)
        compareControl = new CompareControl(typesToInclude)
        compareControl.addSuppressedField(Table.class, "remarks")
        compareControl.addSuppressedField(Column.class, "remarks")
        compareControl.addSuppressedField(Column.class, "certainDataType")
        compareControl.addSuppressedField(Column.class, "autoIncrementInformation")
        compareControl.addSuppressedField(ForeignKey.class, "deleteRule")
        compareControl.addSuppressedField(ForeignKey.class, "updateRule")
        compareControl.addSuppressedField(Index.class, "unique")
    }

    @Unroll
    def "compare referenceDatabase #testInput.referenceDatabase.name #testInput.referenceDatabase.version to #testInput.targetDatabase.name #testInput.targetDatabase.version verify no significant diffs"() {
        given:
        Liquibase liquibase = new Liquibase((String) null, new ClassLoaderResourceAccessor(), testInput.targetDatabase.database)

        DiffResult diffResult = liquibase.diff(testInput.referenceDatabase.database, testInput.targetDatabase.database, compareControl)

        File outFile = File.createTempFile("lb-test", ".xml")
        OutputStream outChangeLog = new FileOutputStream(outFile)
        String changeLogString = toChangeLog(diffResult)
        outChangeLog.write(changeLogString.getBytes("UTF-8"))
        outChangeLog.close()

        Scope.getCurrentScope().getLog(getClass()).info("Changelog:\n" + changeLogString)

        when:

        liquibase = new Liquibase(outFile.toString(), new FileSystemResourceAccessor(File.listRoots()), testInput.targetDatabase.database)
//        StringWriter stringWriter = new StringWriter();
//        liquibase.update(testInput.context, stringWriter)
//        Scope.getCurrentScope().getLog(getClass()).info(stringWriter.toString());
        liquibase.update(testInput.context);

        DiffResult newDiffResult =  liquibase.diff(testInput.referenceDatabase.database, testInput.targetDatabase.database, compareControl)

        ignoreDatabaseChangeLogTable(diffResult)
        ignoreConversionFromFloatToDouble64(diffResult)

        then:

        newDiffResult.getMissingObjects().size() == 0
        newDiffResult.getUnexpectedObjects().size()== 0

        where:
        testInput << buildTestInput()
    }

}
