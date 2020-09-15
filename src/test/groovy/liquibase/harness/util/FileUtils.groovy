package liquibase.harness.util

import groovy.io.FileType
import liquibase.harness.config.TestConfig
import liquibase.harness.config.TestInput
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FileUtils {
    static Logger logger = LoggerFactory.getLogger(FileUtils.class)
    static final String resourceBaseDir = "src/test/resources/"

    static String getFileContent(TestInput testInput, String expectedFolder, String fileExtension) {
        Path databaseSpecificPath = Paths.get(resourceBaseDir, expectedFolder, testInput.databaseName,
                testInput.changeObject+fileExtension)
        Path versionSpecificPath = Paths.get(resourceBaseDir, expectedFolder, testInput.databaseName, testInput.version,
                testInput.changeObject+fileExtension)
        try {
            if(Files.exists(versionSpecificPath)){
                return Files.readString(versionSpecificPath)
            } else {
                return Files.readString(databaseSpecificPath)
            }
        } catch (IOException e) {
            logger.warn(e.getMessage())
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
        if(changeObjects){
            map.putAll(getDefaultChangeObjects(changeObjects, inputFormat))
        } else {
            map.putAll(getAllChangeObjectsFromDir("changelogs/", inputFormat))
        }
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

    static Map<String, String> getAllChangeObjectsFromDir(String pathToDir, String inputFormat) {
        File dir = new File(resourceBaseDir + pathToDir)
        Map<String, String> resultMap = new HashMap<>()
            dir.eachFile(FileType.FILES) { file ->
                if (file.name.endsWith("." + inputFormat)) {
                    resultMap.put(file.getName().substring(0, file.getName().lastIndexOf(".")), file.getPath())
                }
            }
        return resultMap
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
