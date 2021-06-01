package liquibase.harness.diff


import groovy.transform.builder.Builder
import liquibase.diff.DiffResult
import liquibase.diff.Difference
import liquibase.diff.ObjectDifferences
import liquibase.diff.compare.CompareControl
import liquibase.diff.output.DiffOutputControl
import liquibase.diff.output.changelog.DiffToChangeLog
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
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

import java.util.stream.Collectors

import static liquibase.util.StringUtil.isNotEmpty

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
        InputStream testConfig = DiffCommandTestHelper.class.getResourceAsStream("/liquibase/harness/diff/diffDatabases.yml")
        assert testConfig != null: "Cannot find diffDatabases.yml in classpath"

        List<TargetToReference> targetToReferences = configFileYml.loadAs(testConfig, DiffDatabases.class).references

        List<TestInput> inputList = new ArrayList<>()
        List<DatabaseUnderTest> databasesToConnect = new ArrayList<>()
        for (TargetToReference targetToReference : targetToReferences) {
            DatabaseUnderTest targetDatabase
            List<DatabaseUnderTest> matchingTargetDatabases = TestConfig.instance.databasesUnderTest.stream()
                    .filter({ it -> it.name.equalsIgnoreCase(targetToReference.targetDatabaseName) })
                    .collect(Collectors.toList())
            if (matchingTargetDatabases.size() == 1) {
                targetDatabase = matchingTargetDatabases.get(0)
            } else if (matchingTargetDatabases.size() > 1 && isNotEmpty(targetToReference.targetDatabaseVersion)) {
                targetDatabase = matchingTargetDatabases.stream()
                        .filter({ it -> targetToReference.targetDatabaseVersion.equalsIgnoreCase(it.version) })
                        .findFirst()
                        .orElseThrow({ ->
                            new IllegalArgumentException(
                                    String.format("Versions in harness-config.yml don't match with targetDatabaseVersion=%s provided in diffDatabases.yml",
                                            targetToReference.targetDatabaseVersion))
                        })
            } else {
                throw new IllegalArgumentException(String.format("can't match target DB for diff test name={%s}, version={%s}", targetToReference.targetDatabaseName, targetToReference.targetDatabaseVersion))
            }
            databasesToConnect.add(targetDatabase)

            DatabaseUnderTest referenceDatabase
            List<DatabaseUnderTest> matchingReferenceDatabases = TestConfig.instance.databasesUnderTest.stream()
                    .filter({ it -> it.name.equalsIgnoreCase(targetToReference.referenceDatabaseName) })
                    .collect(Collectors.toList())
            if (matchingReferenceDatabases.size() == 1) {
                referenceDatabase = matchingReferenceDatabases.get(0)
            } else if (matchingReferenceDatabases.size() > 1 && isNotEmpty(targetToReference.referenceDatabaseVersion)) {
                referenceDatabase = matchingReferenceDatabases.stream()
                        .filter({ it -> targetToReference.referenceDatabaseVersion.equalsIgnoreCase(it.version) })
                        .findFirst()
                        .orElseThrow({ ->
                            new IllegalArgumentException(
                                    String.format("Versions in harness-config.yml don't match with referenceDatabaseVersion=%s provided in diffDatabases.yml",
                                            targetToReference.referenceDatabaseVersion))
                        })
            } else {
                throw new IllegalArgumentException(String.format("can't match reference DB for diff test name={%s}, version={%s}", targetToReference.referenceDatabaseName, targetToReference.referenceDatabaseVersion))
            }
            databasesToConnect.add(referenceDatabase)

            inputList.add(TestInput.builder()
                    .context(TestConfig.instance.context)
                    .expectedDiffs(targetToReference.expectedDiffs)
                    .targetDatabase(targetDatabase)
                    .referenceDatabase(referenceDatabase)
                    .build())
        }
        DatabaseConnectionUtil databaseConnectionUtil = new DatabaseConnectionUtil()
        databaseConnectionUtil.initializeDatabasesConnection(databasesToConnect)
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

    static void removeExpectedDiffs(ExpectedDiffs expectedDiffs, DiffResult diffResult) {
        removeUnexpectedObjects(diffResult.getUnexpectedObjects(), expectedDiffs.unexpectedObjects)
        removeMissingObjects(diffResult.getMissingObjects(), expectedDiffs.missingObjects)
        removeChangedObjects(diffResult.getChangedObjects(), expectedDiffs.changedObjects)

    }

    static void removeChangedObjects(Map<DatabaseObject, ObjectDifferences> diffResultMap, List<HarnessObjectDifference> expectedChangedObjects) {
        diffResultMap.entrySet().removeIf({ entry -> entry.key.toString().toUpperCase().contains("DATABASECHANGELOG") })
        diffResultMap.entrySet().removeIf({ entry ->
            doesKeyMatches(entry, expectedChangedObjects)
        })

    }

    static boolean doesKeyMatches(Map.Entry<DatabaseObject, ObjectDifferences> entry, List<HarnessObjectDifference> changedObjects) {
        for (HarnessObjectDifference changedObject : changedObjects) {
            if (entry.key.toString().equalsIgnoreCase(changedObject.diffName))
                return matchDifferences(entry, changedObject)
        }
        return false
    }

    static boolean matchDifferences(Map.Entry<DatabaseObject, ObjectDifferences> entry, HarnessObjectDifference harnessObjectDifference) {
        if (entry.value.differences.size() != harnessObjectDifference.diffs.size()) {
            return false
        }
        Map<String, String> transformedObjectDiffMap = new HashMap<>()
        for (Difference difference : entry.value.differences) {
            transformedObjectDiffMap.put(difference.field, difference.message)
        }
        return transformedObjectDiffMap == harnessObjectDifference.diffs
    }

    static void removeMissingObjects(Set<? extends DatabaseObject> diffResultMissingObjects, List<String> missingObjectsFromFile) {
        diffResultMissingObjects.removeIf({ object -> object.toString().toUpperCase().contains("DATABASECHANGELOG") })
        diffResultMissingObjects.removeIf({ object -> missingObjectsFromFile?.contains(object.toString()) })
    }

    static void removeUnexpectedObjects(Set<? extends DatabaseObject> diffResultUnexpectedObjects, List<String> unexpectedObjectsFromFile) {
        diffResultUnexpectedObjects.removeIf({ object -> object.toString().toUpperCase().contains("DATABASECHANGELOG") })
        //TODO figure out why null safe isn't working here
        // diffResultUnexpectedObjects.removeIf({ object -> unexpectedObjectsFromFile?.contains(object.toString()) })
        diffResultUnexpectedObjects.removeIf({ object -> unexpectedObjectsFromFile != null && unexpectedObjectsFromFile.contains(object.toString()) })
    }

    static boolean diffsAbsent(DiffResult diffResult) {
        return diffResult.getChangedObjects()?.isEmpty() && diffResult.getMissingObjects()?.isEmpty() && diffResult.getUnexpectedObjects()?.isEmpty()
    }


    @Builder
    static class TestInput {
        String context
        ExpectedDiffs expectedDiffs
        DatabaseUnderTest referenceDatabase
        DatabaseUnderTest targetDatabase
    }

}
