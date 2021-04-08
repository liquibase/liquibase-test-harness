package liquibase.harness.diff


import groovy.transform.builder.Builder
import liquibase.diff.DiffResult
import liquibase.diff.ObjectDifferences
import liquibase.diff.compare.CompareControl
import liquibase.diff.output.DiffOutputControl
import liquibase.diff.output.changelog.DiffToChangeLog
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.structure.DatabaseObject
import liquibase.structure.core.Column
import liquibase.structure.core.ForeignKey
import liquibase.structure.core.Index
import liquibase.structure.core.PrimaryKey
import liquibase.structure.core.Table
import liquibase.structure.core.Sequence
import liquibase.structure.core.UniqueConstraint
import liquibase.structure.core.View
import org.yaml.snakeyaml.Yaml

class DiffCommandTestHelper {

    static CompareControl buildCompareControl() {
        CompareControl compareControl
        Set<Class<? extends DatabaseObject>> typesToInclude = new HashSet<Class<? extends DatabaseObject>>()
        typesToInclude.add(Table.class)
        typesToInclude.add(Column.class)
        typesToInclude.add(PrimaryKey.class)
        typesToInclude.add(ForeignKey.class)
        typesToInclude.add(UniqueConstraint.class)
        typesToInclude.add(Sequence.class)
        typesToInclude.add(View.class)
        compareControl = new CompareControl(typesToInclude)
        compareControl.addSuppressedField(Table.class, "remarks")
        compareControl.addSuppressedField(Column.class, "remarks")
        compareControl.addSuppressedField(Column.class, "certainDataType")
        compareControl.addSuppressedField(Column.class, "autoIncrementInformation")
        compareControl.addSuppressedField(ForeignKey.class, "deleteRule")
        compareControl.addSuppressedField(ForeignKey.class, "updateRule")
        compareControl.addSuppressedField(Index.class, "unique")
        return compareControl
    }
    static List<TestInput> buildTestInput() {

        Yaml configFileYml = new Yaml()
        InputStream testConfig = getClass().getResourceAsStream("/liquibase/harness/diff/diffDatabases.yml")
        assert testConfig != null : "Cannot find diffDatabases.yml in classpath"

        List<TargetToReference> targetToReferences = configFileYml.loadAs(testConfig, DiffDatabases.class).references

        List<TestInput> inputList = new ArrayList<>()
        for (TargetToReference targetToReference : targetToReferences) {

            DatabaseUnderTest targetDatabase = TestConfig.instance.databasesUnderTest.find{it.name.equalsIgnoreCase(targetToReference.targetDatabaseName)&&
            it.version.equalsIgnoreCase(targetToReference.targetDatabaseVersion)}

            DatabaseUnderTest referenceDatabase = TestConfig.instance.databasesUnderTest.find{it.name.equalsIgnoreCase(targetToReference.referenceDatabaseName)&&
                    it.version.equalsIgnoreCase(targetToReference.referenceDatabaseVersion)}

            inputList.add(TestInput.builder()
                    .context(TestConfig.instance.context)
                    .expectedDiffs(targetToReference.expectedDiffs)
                    .targetDatabase(targetDatabase)
                    .referenceDatabase(referenceDatabase)
                    .build())
            }
        return inputList
    }

    static String toChangeLog(DiffResult diffResult) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        PrintStream printStream = new PrintStream(out, true, "UTF-8")
        DiffOutputControl diffOutputControl = new DiffOutputControl()
        diffOutputControl.setIncludeCatalog(false)
        diffOutputControl.setIncludeSchema(false)
        DiffToChangeLog diffToChangeLog = new DiffToChangeLog(diffResult,
                diffOutputControl)
        diffToChangeLog.print(printStream)
        printStream.close()
        return out.toString("UTF-8")
    }

    static void removeExpectedDiffs(ExpectedDiffs expectedDiffs, DiffResult diffResult){
        removeUnexpectedObjects(diffResult.getUnexpectedObjects(), expectedDiffs)
        removeMissingObjects(diffResult.getMissingObjects(), expectedDiffs)
        removeChangedObjects(diffResult.getChangedObjects(), expectedDiffs)

    }

    static void removeChangedObjects(Map<DatabaseObject, ObjectDifferences> map, ExpectedDiffs expectedDiffs){
         map.entrySet().removeIf({ entry -> expectedDiffs.changedObjects?.contains(entry.key.toString()) })
        map.entrySet().removeIf({ entry -> entry.key.toString().toUpperCase().contains("DATABASECHANGELOG") })
    }

    static void removeMissingObjects(Set<? extends DatabaseObject> missingObjects, ExpectedDiffs expectedDiffs){
        missingObjects.removeIf({ object -> expectedDiffs.changedObjects?.contains(object.toString()) })
        missingObjects.removeIf({ object -> object.toString().toUpperCase().contains("DATABASECHANGELOG") })

    }
    static void removeUnexpectedObjects(Set<? extends DatabaseObject> unexpectedObjects, ExpectedDiffs expectedDiffs){
        unexpectedObjects.removeIf({ object -> expectedDiffs.changedObjects?.contains(object.toString()) })
        unexpectedObjects.removeIf({ object -> object.toString().toUpperCase().contains("DATABASECHANGELOG") })
    }

    @Builder
    static class TestInput {
        String context
        ExpectedDiffs expectedDiffs
        DatabaseUnderTest referenceDatabase
        DatabaseUnderTest targetDatabase
    }

}
