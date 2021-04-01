package liquibase.harness.diff


import groovy.transform.builder.Builder
import liquibase.diff.DiffResult
import liquibase.diff.ObjectDifferences
import liquibase.diff.output.DiffOutputControl
import liquibase.diff.output.changelog.DiffToChangeLog
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.structure.core.Column
import liquibase.structure.core.Index
import liquibase.structure.core.PrimaryKey
import liquibase.structure.core.Table
import liquibase.util.StreamUtil
import org.yaml.snakeyaml.Yaml

class DiffCommandTestHelper {

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

    static boolean removeAndCheckExpectedDiffs(ExpectedDiffs expectedDiffs, DiffResult diffResult){
        //TODO finish this, think bout other objects -views foreignKeys, UniqueConstraints, storedProcedures, sequences
        // refactor to move Predicated outside

        Set<Table> unexpectedTables = diffResult.getUnexpectedObjects(Table.class)
        unexpectedTables.removeIf({ table -> "DATABASECHANGELOGLOCK".equalsIgnoreCase(table.name) || "DATABASECHANGELOG".equalsIgnoreCase(table.name) })
        unexpectedTables.removeIf({ table -> expectedDiffs.unexpectedObjects?.contains(table.name) })

        Set<Table> missingTables = diffResult.getMissingObjects(Table.class)
        missingTables.removeIf({ table -> "DATABASECHANGELOGLOCK".equalsIgnoreCase(table.name) || "DATABASECHANGELOG".equalsIgnoreCase(table.name) })
        missingTables.removeIf({ table -> expectedDiffs.missingObjects?.contains(table.name) })

        Map<Table, ObjectDifferences> changedTables = diffResult.getChangedObjects(Table.class)

        changedTables.entrySet().removeIf({ entry -> expectedDiffs.changedObjects?.contains(entry.key.toString()) })


        Set<Column> unexpectedColumns = diffResult.getUnexpectedObjects(Column.class)
        unexpectedColumns.removeIf({ column -> "DATABASECHANGELOGLOCK".equalsIgnoreCase(column.relation.name) || "DATABASECHANGELOG".equalsIgnoreCase(column.relation.name) })
        unexpectedColumns.removeIf({ column -> expectedDiffs.unexpectedObjects?.contains(column.relation.name) })

        Set<Column> missingColumns = diffResult.getMissingObjects(Column.class)
        missingColumns.removeIf({ column -> "DATABASECHANGELOGLOCK".equalsIgnoreCase(column.relation.name) || "DATABASECHANGELOG".equalsIgnoreCase(column.relation.name) })
        missingColumns.removeIf({ column -> expectedDiffs.missingObjects?.contains(column.relation.name) })

        Map<Column, ObjectDifferences> changedColumns = diffResult.getChangedObjects(Column.class)
        changedColumns.entrySet().removeIf({ entry -> "DATABASECHANGELOGLOCK".equalsIgnoreCase(entry.key.relation.name) || "DATABASECHANGELOG".equalsIgnoreCase(entry.key.relation.name) })
        //TODO does toString() suit our case when diff for column is like "lbcat.posts.id", check other cases
        changedColumns.entrySet().removeIf({ entry -> expectedDiffs.changedObjects?.contains(entry.key.toString()) })


        Set<Index> unexpectedIndexes = diffResult.getUnexpectedObjects(Index.class)
        unexpectedIndexes.removeIf({ index -> "DATABASECHANGELOGLOCK".equalsIgnoreCase(index.relation.name) || "DATABASECHANGELOG".equalsIgnoreCase(index.relation.name) })
        unexpectedIndexes.removeIf({ index -> expectedDiffs.unexpectedObjects?.contains(index.relation.name) })

        Set<Index> missingIndexes = diffResult.getMissingObjects(Index.class)
        missingIndexes.removeIf({ index -> "DATABASECHANGELOGLOCK".equalsIgnoreCase(index.relation.name) || "DATABASECHANGELOG".equalsIgnoreCase(index.relation.name) })
        missingIndexes.removeIf({ index -> expectedDiffs.missingObjects?.contains(index.relation.name) })

        Map<Index, ObjectDifferences> changedIndexes = diffResult.getChangedObjects(Index.class)
        changedIndexes.entrySet().removeIf({ entry -> expectedDiffs.changedObjects.contains(entry.key.relation.name) })


        Set<PrimaryKey> unexpectedPrimaryKeys = diffResult.getUnexpectedObjects(PrimaryKey.class)
        unexpectedPrimaryKeys.removeIf({ primaryKey -> "DATABASECHANGELOGLOCK".equalsIgnoreCase(primaryKey.table.name) || "DATABASECHANGELOG".equalsIgnoreCase(primaryKey.table.name) })
        unexpectedPrimaryKeys.removeIf({ primaryKey -> expectedDiffs.unexpectedObjects?.contains(primaryKey.table.name) })

        Set<PrimaryKey> missingPrimaryKeys = diffResult.getMissingObjects(PrimaryKey.class)
        missingPrimaryKeys.removeIf({ primaryKey -> "DATABASECHANGELOGLOCK".equalsIgnoreCase(primaryKey.table.name) || "DATABASECHANGELOG".equalsIgnoreCase(primaryKey.table.name) })
        missingPrimaryKeys.removeIf({ primaryKey -> expectedDiffs.unexpectedObjects?.contains(primaryKey.table.name) })

        Map<PrimaryKey, ObjectDifferences> changedPrimaryKeys = diffResult.getChangedObjects(PrimaryKey.class)
        changedPrimaryKeys.entrySet().removeIf({ entry -> expectedDiffs.changedObjects?.contains(entry.key.table.name) })

        return unexpectedTables.size() == 0 &&
        missingTables.size() == 0 &&
        changedTables.size() == 0 &&
        unexpectedColumns.size() == 0 &&
        missingColumns.size() == 0 &&
        changedColumns.size() == 0 &&
        unexpectedIndexes.size() == 0 &&
        missingIndexes.size() == 0 &&
        changedIndexes.size() == 0 &&
        unexpectedPrimaryKeys.size() == 0 &&
        missingPrimaryKeys.size() == 0 &&
        changedPrimaryKeys.size() == 0
    }

    @Builder
    static class TestInput {
        String context
        ExpectedDiffs expectedDiffs
        DatabaseUnderTest referenceDatabase
        DatabaseUnderTest targetDatabase
    }

}
