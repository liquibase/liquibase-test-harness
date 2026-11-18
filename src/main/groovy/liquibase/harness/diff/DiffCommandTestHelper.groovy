package liquibase.harness.diff

import groovy.transform.builder.Builder
import liquibase.Scope
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.DatabaseConnectionUtil
import liquibase.ui.UIService
import org.yaml.snakeyaml.Yaml

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

            String baseChangelogName = "${referenceDatabase.name}${referenceDatabase.version}_to_" +
                    "${targetDatabase.name}${targetDatabase.version}"
            String changelogFile = selectChangelogFile(baseChangelogName)

            String expectedDiffFile = selectExpectedDiffFile(baseChangelogName)

            inputList.add(TestInput.builder()
                    .pathToExpectedDiffFile(expectedDiffFile)
                    .pathToReferenceChangelogFile("${baseDiffPath}" + "changelogs/" +
                            "tablesForReferenceDb.xml")
                    .pathToChangelogFile(changelogFile)
                    .targetDatabase(targetDatabase)
                    .referenceDatabase(referenceDatabase)
                    .build())
        }
        new DatabaseConnectionUtil().initializeDatabasesConnection(databasesToConnect)
        return inputList
    }

    static String selectChangelogFile(String baseChangelogName) {
        if ("true".equalsIgnoreCase(System.getProperty("useProArtifacts"))) {
            return "${baseDiffPath}changelogs/${baseChangelogName}-pro.xml"
        }
        return "${baseDiffPath}changelogs/${baseChangelogName}.xml"
    }

    static String selectExpectedDiffFile(String baseChangelogName) {
        if ("true".equalsIgnoreCase(System.getProperty("useProArtifacts"))) {
            return "${baseDiffPath}expectedDiff/${baseChangelogName}-pro.txt"
        }
        return "${baseDiffPath}expectedDiff/${baseChangelogName}.txt"
    }

    static String removeDatabaseInfoFromDiff(String diff) {
        String replacementRegexpRef = "Reference Database(.*?)\r?\n" //removes Reference Database diff line to generalize test data
        String replacementRegexpComp = "Comparison Database(.*?)\r?\n" //removes Comparison Database diff line to generalize test data
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

    @Builder
    static class TestInput {
        String pathToExpectedDiffFile
        String pathToChangelogFile
        String pathToReferenceChangelogFile
        DatabaseUnderTest referenceDatabase
        DatabaseUnderTest targetDatabase
    }
}
