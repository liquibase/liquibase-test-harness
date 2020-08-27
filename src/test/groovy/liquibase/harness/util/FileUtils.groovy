package liquibase.harness.util

import groovy.io.FileType
import liquibase.harness.config.TestConfig
import liquibase.harness.config.TestInput
import org.yaml.snakeyaml.Yaml

class FileUtils {
    static final String resourceBaseDir = "src/test/resources/"

    static String getFileContent(TestInput testInput, String expectedFolder, String fileExtension) {
        try {
            return new File(new StringBuilder(resourceBaseDir)
                    .append(expectedFolder)
                    .append("/")
                    .append(testInput.databaseName)
                    .append("/")
                    .append(testInput.changeObject)
                    .append(fileExtension)
                    .toString()
            ).getText("UTF-8")
        } catch (IOException e) {
            return null
        }
    }

    static String getExpectedSqlFileContent(TestInput testInput) {
        return getFileContent(testInput, "expectedSql", ".sql")
    }

    static String getExpectedSnapshotFileContent(TestInput testInput) {
        return getFileContent(testInput, "expectedSnapshot", ".json")
    }

    static TestConfig readYamlConfig(String fileName) {
        Yaml configFileYml = new Yaml()
        return configFileYml.loadAs(new File(resourceBaseDir, fileName).newInputStream(), TestConfig.class)
    }

    static Map<String, String> getDefaultChangeObjects(List<String> changeObjects, String inputFormat) {
        return getChangeObjects(changeObjects, "changelogs/", inputFormat)
    }


    static Map<String, String> getDatabaseSpecificChangeObjects(List<String> changeObjects, String databaseName, inputFormat) {
        return getChangeObjects(changeObjects, "changelogs/" + databaseName, inputFormat)
    }

    static Map<String, String> getVersionSpecificChangeObjects(List<String> changeObjects, String databaseName, String dbVersion, String inputFormat) {
        return getChangeObjects(changeObjects, new StringBuilder("changelogs/")
                .append(databaseName)
                .append("/")
                .append(dbVersion)
                .toString(),
                inputFormat)
    }

    static Map<String, String> getChangeObjects(List<String> changeObjects, String pathToDir, String inputFormat) {
        Map<String, String> changeObjectsMap = new HashMap<>()
        def dir = new File(resourceBaseDir + pathToDir)
        for (String changeObject : changeObjects) {
            dir.eachFile(FileType.FILES) { file ->
                if (file.name.matches(changeObject + "." + inputFormat)) {
                    changeObjectsMap.put(changeObject, file.getPath())
                }
            }
        }
        return changeObjectsMap
    }

}
