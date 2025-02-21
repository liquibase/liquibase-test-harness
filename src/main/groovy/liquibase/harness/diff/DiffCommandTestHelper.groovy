package liquibase.harness.diff

import groovy.transform.builder.Builder
import liquibase.Scope
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.ui.UIService
import org.apache.commons.io.FileUtils
import org.yaml.snakeyaml.Yaml

import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

import static liquibase.harness.util.FileUtils.*
import static liquibase.util.StringUtil.isNotEmpty

class DiffCommandTestHelper {

    final static UIService uiService = Scope.getCurrentScope().getUI()
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
                    .pathToExpectedDiffFile("${baseDiffPath}" + "expectedDiff/" +
                            "${referenceDatabase.name}${referenceDatabase.version}_to_" +
                            "${targetDatabase.name}${targetDatabase.version}.txt")
                    .pathToReferenceChangelogFile("${baseDiffPath}" + "changelogs/" +
                            "tablesForReferenceDb.xml")
                    .pathToChangelogFile("${baseDiffPath}" + "changelogs/" +
                            "${referenceDatabase.name}${referenceDatabase.version}_to_" +
                            "${targetDatabase.name}${targetDatabase.version}.xml")
                    .pathToGeneratedXmlDiffChangelogFile("src/main/resources${baseDiffPath}" + "expectedDiffChangelogs/" +
                            "${referenceDatabase.name}${referenceDatabase.version}_to_" +
                            "${targetDatabase.name}${targetDatabase.version}.xml")
                    .pathToGeneratedSqlDiffChangelogFile("src/main/resources${baseDiffPath}" + "expectedDiffChangelogs/" +
                            "${referenceDatabase.name}${referenceDatabase.version}_to_" +
                            "${targetDatabase.name}${targetDatabase.version}.sql")
                    .pathToGeneratedYmlDiffChangelogFile("src/main/resources${baseDiffPath}" + "expectedDiffChangelogs/" +
                            "${referenceDatabase.name}${referenceDatabase.version}_to_" +
                            "${targetDatabase.name}${targetDatabase.version}.yml")
                    .pathToGeneratedJsonDiffChangelogFile("src/main/resources${baseDiffPath}" + "expectedDiffChangelogs/" +
                            "${referenceDatabase.name}${referenceDatabase.version}_to_" +
                            "${targetDatabase.name}${targetDatabase.version}.json")
                    .targetDatabase(targetDatabase)
                    .referenceDatabase(referenceDatabase)
                    .pathToExpectedDiffFolder("src/main/resources${baseDiffPath}" + "expectedDiffChangelogs/")
                    .build())
        }
        new DatabaseConnectionUtil().initializeDatabasesConnection(databasesToConnect)
        return inputList
    }

    static validateGeneratedDiffChangelog(TestInput testInput) {
        String changelogContent = getResourceContent("$testInput.pathToChangelogFile")
        String xmlDiffChangelogContent = readFile(testInput.getPathToGeneratedXmlDiffChangelogFile())
        String sqlDiffChangelogContent = readFile(testInput.getPathToGeneratedSqlDiffChangelogFile().replace(".sql",
                ".$testInput.targetDatabase.name" + ".sql"))
        String ymlDiffChangelogContent = readFile(testInput.getPathToGeneratedYmlDiffChangelogFile())
        String jsonDiffChangelogContent = readFile(testInput.getPathToGeneratedJsonDiffChangelogFile())

        return validateChangeTypes(changelogContent, xmlDiffChangelogContent, sqlDiffChangelogContent,
                ymlDiffChangelogContent, jsonDiffChangelogContent, testInput.referenceDatabase.name)
    }

    private static Boolean validateChangeTypes(String changelogContent, String xmlDiffChangelogContent,
                                               String sqlDiffChangelogContent, String ymlDiffChangelogContent,
                                               String jsonDiffChangelogContent, String dbName) {
        def map = new LinkedHashMap<String, List>()
        map.put("createTable", new ArrayList<>(List.of("dropTable", "drop table")))
        map.put("createView", new ArrayList<>(List.of("dropView", "drop view")))
        map.put("createIndex", new ArrayList<>(List.of("dropIndex", "drop index")))
        map.put("createSequence", new ArrayList<>(List.of("dropSequence", "drop sequence")))
        map.put("createFunction", new ArrayList<>(List.of("dropFunction", "drop function")))
        map.put("createTrigger", new ArrayList<>(List.of("dropTrigger", "drop trigger")))
        map.put("createProcedure", new ArrayList<>(List.of("dropProcedure", "drop procedure")))

        if(dbName.equals("mariadb") || dbName.equals("mysql")) {
            map.put("addForeignKey", new ArrayList<>(List.of("dropForeignKey", "drop foreign key")))
            map.put("addPrimaryKey", new ArrayList<>(List.of("dropPrimaryKey", "drop key")))
            map.put("addUniqueConstraint", new ArrayList<>(List.of("dropUniqueConstraint", "drop key")))
        } else {
            map.put("addForeignKey", new ArrayList<>(List.of("dropForeignKey", "drop constraint")))
            map.put("addPrimaryKey", new ArrayList<>(List.of("dropPrimaryKey", "drop constraint")))
            map.put("addUniqueConstraint", new ArrayList<>(List.of("dropUniqueConstraint", "drop constraint")))
        }

        //Schema and Catalog to add. Also this will probably change while adding new types
        for (Map.Entry<String, List> entry : map.entrySet()) {
            if (changelogContent.contains(entry.key)) {
                assert xmlDiffChangelogContent.contains(entry.value.get(0).toString()) &&
                        sqlDiffChangelogContent.toLowerCase().contains(entry.value.get(1).toString())
                        ymlDiffChangelogContent.contains(entry.value.get(0).toString()) &&
                        jsonDiffChangelogContent.contains(entry.value.get(0).toString()) &&

                uiService.sendMessage("INFO: $entry.key change type was validated successfully for .XML, .YML, .JSON formats!")
            }
        }
        return true
    }

    static String removeDatabaseInfoFromDiff(String diff) {
        String replacementRegexpRef = "Reference Database(.*?)\r?\n" //removes Reference Database diff line to generalize test data
        String replacementRegexpComp = "Comparison Database(.*?)\r?\n" //removes Comparison Database diff line to generalize test data        String replacementRegexpRef = "Reference Database(.*?)\r?\n" //removes Reference Database diff line to generalize test data
        String replacementRegexpRefDbVersion = "Reference:\\s*'.*'\r?\n"
        String replacementRegexpTargetDbVersion = "Target:\\s*'.*'\r?\n"
        String replacementRegexpWS = "\\s+" //removes whitespaces
        return diff
                .replaceAll(replacementRegexpRef, "")
                .replaceAll(replacementRegexpComp, "")
                .replaceAll(replacementRegexpRefDbVersion, "")
                .replaceAll(replacementRegexpTargetDbVersion, "")
                .replaceAll(replacementRegexpWS, "")
    }

    static void clearFolder(String pathToObjectDir) {
        Path path = Paths.get(pathToObjectDir)
        if (path.toFile().isDirectory()) {
            FileUtils.forceDelete(new File(pathToObjectDir))
        }
    }

    @Builder
    static class TestInput {
        String pathToExpectedDiffFile
        String pathToChangelogFile
        String pathToReferenceChangelogFile
        String pathToGeneratedXmlDiffChangelogFile
        String pathToGeneratedSqlDiffChangelogFile
        String pathToGeneratedYmlDiffChangelogFile
        String pathToGeneratedJsonDiffChangelogFile
        String pathToExpectedDiffFolder
        DatabaseUnderTest referenceDatabase
        DatabaseUnderTest targetDatabase
    }
}
