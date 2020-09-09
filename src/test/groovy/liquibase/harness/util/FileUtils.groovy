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

    static Map<String, String> collectChangeObjects(List<String> changeObjects, List<String> dbSpecificChangeObjects, String databaseName, String dbVersion, String inputFormat) {
        Map<String, String> map = getDatabaseSpecificChangeObjects(dbSpecificChangeObjects, databaseName, dbVersion, inputFormat)
        if (!changeObjects && dbSpecificChangeObjects) {
            return map
            //if someone want to run just a few specific tests don't add all default, return only dbSpecific ones
        }
        map.putAll(getDefaultChangeObjects(changeObjects, inputFormat))
        return map
    }

    static Map<String, String> getDefaultChangeObjects(List<String> changeObjects, String inputFormat) {
        return getChangeObjectsFromDir(changeObjects, "changelogs/", inputFormat)
    }

    static Map<String, String> getDatabaseSpecificChangeObjects(List<String> changeObjects, String databaseName, String dbVersion, String inputFormat) {
        Map<String, String> mergedMap = getChangeObjectsFromDir(changeObjects, "changelogs/" + databaseName, inputFormat)
        mergedMap.putAll(getVersionSpecificChangeObjects(changeObjects, databaseName, dbVersion, inputFormat) as Map)
        return mergedMap
    }

    static Map<String, String> getVersionSpecificChangeObjects(List<String> changeObjects, String databaseName, String dbVersion, String inputFormat) {
        return getChangeObjectsFromDir(changeObjects, new StringBuilder("changelogs/")
                .append(databaseName)
                .append("/")
                .append(dbVersion)
                .toString(),
                inputFormat)
    }

    static Map<String, String> getChangeObjectsFromDir(List<String> changeObjects, String pathToDir, String inputFormat) {
        File dir = new File(resourceBaseDir + pathToDir)
        Map<String, String> resultMap = new HashMap<>()
        for(String changeObject : changeObjects){
            dir.eachFile(FileType.FILES) { file ->
                if (file.name.endsWith((changeObject ?: "") + "." + inputFormat)) {
                    resultMap.put(file.getName().substring(0, file.getName().lastIndexOf(".")), file.getPath())
                }
            }
        }
        return resultMap
    }
}
