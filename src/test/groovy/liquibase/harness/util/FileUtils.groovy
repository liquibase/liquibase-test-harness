package liquibase.harness.util

import groovy.io.FileType
import liquibase.harness.config.TestConfig
import liquibase.harness.config.TestInput
import org.yaml.snakeyaml.Yaml

class FileUtils {
    static final String resourceBaseDir = "src/test/resources/"

    static String getFileContent (TestInput testInput, String expectedFolder, String fileExtension){
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
        } catch (IOException e){
            return null
        }
    }

    static String getExpectedSqlFileContent(TestInput testInput) {
        return getFileContent(testInput,"expectedSql",".sql")
    }

    static String getExpectedSnapshotFileContent(TestInput testInput) {
        return getFileContent(testInput,"expectedSnapshot",".json")
    }

    static TestConfig readYamlConfig(String fileName) {
        Yaml configFileYml = new Yaml()
        return configFileYml.loadAs(new File(resourceBaseDir, fileName).newInputStream(), TestConfig.class)
    }

    static Map<String, String> getDefaultChangeObjects(List<String> changeObjects) {
        return getChangeObjects(changeObjects, "changelogs/");
    }


    static Map<String, String> getDatabaseSpecificChangeObjects(List<String> changeObjects, String databaseName) {
        return getChangeObjects(changeObjects, "changelogs/" + databaseName);
    }

    static Map<String, String> getChangeObjects(List<String> changeObjects, String pathToDir) {
        Map<String, String> changeObjectsMap = new HashMap<>()
        def dir = new File(resourceBaseDir + pathToDir)
        for (String changeObject : changeObjects) {
            dir.eachFile(FileType.FILES) { file ->
                if (file.name.matches(changeObject + ".*")) {
                    changeObjectsMap.put(changeObject, file.getPath())
                }
            }
        }
        return changeObjectsMap
    }

    static Map<String, String> getVersionSpecificChangeObjects(List<String> changeObjects, String databaseName, String dbVersion) {
        return getChangeObjects(changeObjects, new StringBuilder("changelogs/")
                .append(databaseName)
                .append("/")
                .append(dbVersion)
                .toString()
        )
//        Map<String, String> changeTypes = new HashMap<>()
//        def dir = new File(new StringBuilder(resourceBaseDir)
//                .append("changelogs/")
//                .append(databaseName)
//                .append("/")
//                .append(dbVersion)
//                .toString())
//        dir.eachFileRecurse(FileType.FILES) { file ->
//            changeTypes.put(file.getName().substring(0, file.getName().lastIndexOf('.')),file.getPath())
//        }
//        return changeTypes
    }

    static Map<String, String> mapChangeObjectsToFilePaths(List<String> strings) {
        Map<String, String> changeTypeToFilePathMap = new HashMap<>()

        def dir = new File(resourceBaseDir + "changelogs/")
        for (String changeObject : strings) {
            dir.eachFileRecurse(FileType.FILES) { file ->
                if (file.name.matches(changeObject + ".*")) {
                    changeTypeToFilePathMap.put(changeObject, file.getPath())
                }
            }
        }
        return changeTypeToFilePathMap
    }
}
