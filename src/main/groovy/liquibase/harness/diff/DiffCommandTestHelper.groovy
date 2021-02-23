package liquibase.harness.diff


import groovy.transform.builder.Builder
import liquibase.Scope
import liquibase.diff.DiffResult
import liquibase.diff.Difference
import liquibase.diff.ObjectDifferences
import liquibase.diff.output.DiffOutputControl
import liquibase.diff.output.changelog.DiffToChangeLog
import liquibase.diff.output.report.DiffToReport
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.structure.DatabaseObject
import liquibase.structure.core.Column
import liquibase.structure.core.Index
import liquibase.structure.core.PrimaryKey
import liquibase.structure.core.Table
import org.yaml.snakeyaml.Yaml

class DiffCommandTestHelper {

    static List<TestInput> buildTestInput() {

        Yaml configFileYml = new Yaml()
        InputStream testConfig = getClass().getResourceAsStream("/liquibase/harness/diff/databaseMap.yml")
        assert testConfig != null : "Cannot find databaseMap.yml in classpath"

        List<TargetToReference> targetToReferences = configFileYml.loadAs(testConfig, DiffDatabases.class).references

        List<TestInput> inputList = new ArrayList<>()
        for (TargetToReference targetToReference : targetToReferences) {

            DatabaseUnderTest targetDatabase = TestConfig.instance.databasesUnderTest.find{it.name.equalsIgnoreCase(targetToReference.targetDatabaseName)&&
            it.version.equalsIgnoreCase(targetToReference.targetDatabaseVersion)}

            DatabaseUnderTest referenceDatabase = TestConfig.instance.databasesUnderTest.find{it.name.equalsIgnoreCase(targetToReference.referenceDatabaseName)&&
                    it.version.equalsIgnoreCase(targetToReference.referenceDatabaseVersion)}

            inputList.add(TestInput.builder()
                    .context(TestConfig.instance.context)
                    .targetDatabase(targetDatabase)
                    .referenceDatabase(referenceDatabase)
                    .build())
            }
        return inputList
    }

    static String diffResultToString(DiffResult diffResult) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        PrintStream printStream = new PrintStream(out, true, "UTF-8")
        DiffToReport diffToReport = new DiffToReport(diffResult, printStream)
        diffToReport.print()
        printStream.close()
        return out.toString("UTF-8")
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

    static void ignoreDatabaseChangeLogTable(DiffResult diffResult) throws Exception {
        Set<Table> unexpectedTables = diffResult.getUnexpectedObjects(Table.class)
        for (Iterator<Table> iterator = unexpectedTables.iterator(); iterator.hasNext(); ) {
            Table table = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(table.getName()) || "DATABASECHANGELOG".equalsIgnoreCase(table.getName()))
                diffResult.getUnexpectedObjects().remove(table)
        }
        Set<Table> missingTables = diffResult.getMissingObjects(Table.class)
        for (Iterator<Table> iterator = missingTables.iterator(); iterator.hasNext(); ) {
            Table table = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(table.getName()) || "DATABASECHANGELOG".equalsIgnoreCase(table.getName()))
                diffResult.getMissingObjects().remove(table)
        }
        Set<Column> unexpectedColumns = diffResult.getUnexpectedObjects(Column.class)
        for (Iterator<Column> iterator = unexpectedColumns.iterator(); iterator.hasNext(); ) {
            Column column = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(column.getRelation().getName()) || "DATABASECHANGELOG".equalsIgnoreCase(column.getRelation().getName()))
                diffResult.getUnexpectedObjects().remove(column)
        }
        Set<Column> missingColumns = diffResult.getMissingObjects(Column.class)
        for (Iterator<Column> iterator = missingColumns.iterator(); iterator.hasNext(); ) {
            Column column = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(column.getRelation().getName()) || "DATABASECHANGELOG".equalsIgnoreCase(column.getRelation().getName()))
                diffResult.getMissingObjects().remove(column)
        }
        Set<Index> unexpectedIndexes = diffResult.getUnexpectedObjects(Index.class)
        for (Iterator<Index> iterator = unexpectedIndexes.iterator(); iterator.hasNext(); ) {
            Index index = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(index.getTable().getName()) || "DATABASECHANGELOG".equalsIgnoreCase(index.getTable().getName()))
                diffResult.getUnexpectedObjects().remove(index)
        }
        Set<Index> missingIndexes = diffResult.getMissingObjects(Index.class)
        for (Iterator<Index> iterator = missingIndexes.iterator(); iterator.hasNext(); ) {
            Index index = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(index.getTable().getName()) || "DATABASECHANGELOG".equalsIgnoreCase(index.getTable().getName()))
                diffResult.getMissingObjects().remove(index)
        }
        Set<PrimaryKey> unexpectedPrimaryKeys = diffResult.getUnexpectedObjects(PrimaryKey.class)
        for (Iterator<PrimaryKey> iterator = unexpectedPrimaryKeys.iterator(); iterator.hasNext(); ) {
            PrimaryKey primaryKey = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(primaryKey.getTable().getName()) || "DATABASECHANGELOG".equalsIgnoreCase(primaryKey.getTable().getName()))
                diffResult.getUnexpectedObjects().remove(primaryKey)
        }
        Set<PrimaryKey> missingPrimaryKeys = diffResult.getMissingObjects(PrimaryKey.class)
        for (Iterator<PrimaryKey> iterator = missingPrimaryKeys.iterator(); iterator.hasNext(); ) {
            PrimaryKey primaryKey = iterator.next()
            if ("DATABASECHANGELOGLOCK".equalsIgnoreCase(primaryKey.getTable().getName()) || "DATABASECHANGELOG".equalsIgnoreCase(primaryKey.getTable().getName()))
                diffResult.getMissingObjects().remove(primaryKey)
        }
    }

    /**
     * Columns created as float are seen as DOUBLE(64) in database metadata.
     * HsqlDB bug?
     *
     * @param diffResult
     * @throws Exception
     */
    static void ignoreConversionFromFloatToDouble64(DiffResult diffResult) throws Exception {
        Map<DatabaseObject, ObjectDifferences> differences = diffResult.getChangedObjects()
        for (Iterator<Map.Entry<DatabaseObject, ObjectDifferences>> iterator = differences.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<DatabaseObject, ObjectDifferences> changedObject = iterator.next()
            Difference difference = changedObject.getValue().getDifference("type")
            if (difference != null && difference.getReferenceValue() != null && difference.getComparedValue() != null
                    && difference.getReferenceValue().toString() == "float" && difference.getComparedValue().toString().startsWith("DOUBLE(64)")) {
                Scope.getCurrentScope().getLog(getClass()).info("Ignoring difference " + changedObject.getKey().toString() + " " + difference.toString())
                changedObject.getValue().removeDifference(difference.getField())
            }
        }
    }

    @Builder
    static class TestInput {
//        String databaseName
//        String url
//        String dbSchema
//        String username
//        String password
//        String version
        String context
//        String changeObject
//        String pathToChangeLogFile
        DatabaseUnderTest referenceDatabase
        DatabaseUnderTest targetDatabase
    }

}
