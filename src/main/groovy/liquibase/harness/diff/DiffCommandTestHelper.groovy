package liquibase.harness.diff

import groovy.transform.builder.Builder
import liquibase.exception.LiquibaseException
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.harness.util.TestUtils
import org.json.JSONArray
import org.json.JSONObject
import org.yaml.snakeyaml.Yaml
import java.util.logging.Logger
import java.util.stream.Collectors

import static liquibase.util.StringUtil.isNotEmpty

class DiffCommandTestHelper {

    final static String baseDiffPath = "/liquibase/harness/diff/"

    static List<TestInput> buildTestInput() {
        InputStream testConfig = DiffCommandTestHelper.class.getResourceAsStream("${baseDiffPath}diffDatabases.yml")
        assert testConfig != null : "Cannot find diffDatabases.yml in classpath"

        List<TestInput> inputList = new ArrayList<>()
        List<DatabaseUnderTest> databasesToConnect = new ArrayList<>()

        for (TargetToReference targetToReference : new Yaml().loadAs(testConfig, DiffDatabases.class).references) {
            DatabaseUnderTest targetDatabase
            List<DatabaseUnderTest> matchingTargetDatabases = TestConfig.instance.getFilteredDatabasesUnderTest().stream()
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
                                    String.format("Versions in harness-config.yml don't match to targetDatabaseVersion=%s " +
                                            "provided in diffDatabases.yml", targetToReference.targetDatabaseVersion))
                        })
            } else {
                throw new IllegalArgumentException(String.format("can't match target DB for diff test name={%s}, version={%s}",
                        targetToReference.targetDatabaseName, targetToReference.targetDatabaseVersion))
            }
            databasesToConnect.add(targetDatabase)

            DatabaseUnderTest referenceDatabase
            List<DatabaseUnderTest> matchingReferenceDatabases = TestConfig.instance.getFilteredDatabasesUnderTest().stream()
                    .filter({ it -> it.name.equalsIgnoreCase(targetToReference.referenceDatabaseName) })
                    .collect(Collectors.toList())
            if (matchingReferenceDatabases.size() == 1) {
                referenceDatabase = matchingReferenceDatabases.get(0)
            } else if (matchingReferenceDatabases.size() > 1 && isNotEmpty(targetToReference.referenceDatabaseVersion)) {
                referenceDatabase = matchingReferenceDatabases.stream()
                        .filter({it -> targetToReference.referenceDatabaseVersion.equalsIgnoreCase(it.version)})
                        .findFirst()
                        .orElseThrow({ ->
                            new IllegalArgumentException(
                                    String.format("Versions in harness-config.yml don't match with referenceDatabaseVersion=%s " +
                                            "provided in diffDatabases.yml",
                                            targetToReference.referenceDatabaseVersion))
                        })
            } else {
                throw new IllegalArgumentException(String.format("can't match reference DB for diff test name={%s}, version={%s}",
                        targetToReference.referenceDatabaseName, targetToReference.referenceDatabaseVersion))
            }
            databasesToConnect.add(referenceDatabase)

            inputList.add(TestInput.builder()
                    .pathToChangelogFile("src/main/resources${baseDiffPath}" +
                            "${referenceDatabase.name}${referenceDatabase.version}_to_" +
                            "${targetDatabase.name}${targetDatabase.version}.xml")
                    .targetDatabase(targetDatabase)
                    .referenceDatabase(referenceDatabase)
                    .build())
        }
        new DatabaseConnectionUtil().initializeDatabasesConnection(databasesToConnect)
        return inputList
    }

    static String getExpectedDiffPath(TestInput testInput) {
        return "${baseDiffPath}expectedDiff/${testInput.referenceDatabase.name}${testInput.referenceDatabase.version}" +
                "_to_${testInput.targetDatabase.name}${testInput.targetDatabase.version}.json"
    }

    /** This method creates diffToCompare without JSONObjects referenced to databasechangelog* tables
     because --excludeObjects flag doesn't work with --format=json extension for 'diff' command
     */
    static JSONObject createDiffToCompare(OutputStream diffOutput) {
        def generatedDiff = new JSONObject(diffOutput.toString().replaceAll("!!int", ""))
                .getJSONObject("diff")// Replacement because of a bug in diff --format=json generation
        def diffToCompare = new JSONObject()
        def arrays = ["missingObjects", "unexpectedObjects", "changedObjects"]
        def objects = ["missingObject", "unexpectedObject", "changedObject"]
        for (int i = 0; i < arrays.size(); i++) {
            if (generatedDiff.has(arrays[i])) {
                def generatedDiffObjects = generatedDiff.getJSONArray(arrays[i])
                def diffToCompareObjects = new JSONArray()
                for (int j = 0; j < generatedDiffObjects.length(); j++) {
                    def object = generatedDiffObjects.getJSONObject(j).getJSONObject(objects[i])
                    if (object.has("name") && object.getString("name")
                            .toLowerCase().contains("databasechangelog")
                            || object.has("relationName") && object.getString("relationName")
                            .toLowerCase().contains("databasechangelog")) {
                        continue
                    }
                    def checkedObject = new JSONObject()
                    checkedObject.put(objects[i], object)
                    diffToCompareObjects.put(checkedObject)
                }
                if (diffToCompareObjects.length() > 0) {
                    diffToCompare.put(arrays[i], diffToCompareObjects)
                }
            }
        }
        return diffToCompare
    }

    static void tryToRollbackDiff(Map argsMap) {
        try {
            TestUtils.executeCommandScope("rollbackToDate", argsMap)
        } catch (LiquibaseException exception) {
            Logger.getLogger(this.class.name).warning("Failed to rollback changes from generated diff changelog! " +
                    "State of the target database will remain changed! \n" + exception.message + "\n" +
                    exception.printStackTrace())
        }
    }

    @Builder
    static class TestInput {
        String pathToChangelogFile
        DatabaseUnderTest referenceDatabase
        DatabaseUnderTest targetDatabase
    }
}
