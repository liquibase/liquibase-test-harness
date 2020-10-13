package liquibase.sdk.test.util

import groovy.io.FileType
import liquibase.sdk.test.config.TestConfig
import liquibase.sdk.test.config.TestInput
import liquibase.util.StreamUtil
import org.yaml.snakeyaml.Yaml

import java.nio.file.Path
import java.nio.file.Paths

class FileUtils {

    static String getFileContent(TestInput testInput, String expectedFolder, String fileExtension) {
        def content = TestUtils.resourceAccessor.openStream(null, expectedFolder + "/" + testInput.databaseName + "/" + testInput.version + "/" + testInput.changeObject + fileExtension)
        if (content != null) {
            return StreamUtil.readStreamAsString(content)
        }

        content = TestUtils.resourceAccessor.openStream(null, expectedFolder + "/" + testInput.databaseName + "/" + testInput.changeObject + fileExtension)
        if (content != null) {
            return StreamUtil.readStreamAsString(content)
        }

        return null
    }

    static String getExpectedSqlFileContent(TestInput testInput) {
        return getFileContent(testInput, "liquibase/sdk/test/expectedSql", ".sql")
    }

    static String getExpectedSnapshotFileContent(TestInput testInput) {
        return getFileContent(testInput, "liquibase/sdk/test/expectedSnapshot", ".json")
    }

    static TestConfig readYamlConfig(String fileName) {
        Yaml configFileYml = new Yaml()
        return configFileYml.loadAs(getClass().getResourceAsStream(fileName), TestConfig.class)
    }

    static Map<String, String> collectChangeObjects(List<String> changeObjects, List<String> dbSpecificChangeObjects,
                                                    String databaseName, String dbVersion, String inputFormat) {
        Map<String, String> map = new HashMap<>()
        if (dbSpecificChangeObjects) {
            map.putAll(getDatabaseSpecificChangeObjects(dbSpecificChangeObjects, databaseName, dbVersion, inputFormat))
        }
        if (!changeObjects && dbSpecificChangeObjects) {
            //if someone want to run just a few specific tests don't add all default, return only dbSpecific ones
            return map
        }
        changeObjects ? map.putAll(getSpecifiedChangeObjects(changeObjects, databaseName, dbVersion, inputFormat)) :
                map.putAll(getAllChangeObjectsFromDir(Paths.get(getClass().getResource("/changelogs").toURI()), inputFormat))
        return map
    }

    static Map<String, String> getSpecifiedChangeObjects(List<String> changeObjects, String databaseName, String version,
                                                         String inputFormat) {
        Map<String, String> resultMap = getChangeObjectsFromDir(changeObjects, Paths.get(getClass().getResource("/changelogs").toURI()), inputFormat)
        resultMap.putAll(getDatabaseSpecificChangeObjects(changeObjects, databaseName, version, inputFormat))
        return resultMap
    }

    static Map<String, String> getDatabaseSpecificChangeObjects(List<String> changeObjects, String databaseName,
                                                                String dbVersion, String inputFormat) {
        Map<String, String> mergedMap = getChangeObjectsFromDir(changeObjects,
                Paths.get(getClass().getResource("/changelogs").toURI()).resolve(databaseName), inputFormat)
        mergedMap.putAll(getVersionSpecificChangeObjects(changeObjects, databaseName, dbVersion, inputFormat) as Map)
        return mergedMap
    }

    static Map<String, String> getVersionSpecificChangeObjects(List<String> changeObjects, String databaseName, String dbVersion, String inputFormat) {
        return getChangeObjectsFromDir(changeObjects,
                Paths.get(getClass().getResource("/changelogs").toURI()).resolve(Paths.get(databaseName, dbVersion)), inputFormat)
    }

    static Map<String, String> getAllChangeObjectsFromDir(Path pathToDir, String inputFormat) {
        File dir = new File(pathToDir.toString())
        Map<String, String> resultMap = new HashMap<>()
        dir.eachFile(FileType.FILES) { file ->
            if (file.name.endsWith("." + inputFormat)) {
                resultMap.put(file.getName().substring(0, file.getName().lastIndexOf(".")), file.getPath())
            }
        }
        return resultMap
    }

    static Map<String, String> getChangeObjectsFromDir(List<String> changeObjects, Path pathToDir, String inputFormat) {
        File dir = new File(pathToDir.toString()) //TODO rewrite this in NIO style
        Map<String, String> resultMap = new HashMap<>()
        for (String changeObject : changeObjects) {
            dir.eachFile(FileType.FILES) { file ->
                if (file.name.endsWith((changeObject ?: "") + "." + inputFormat)) {
                    pathToDir.resolve(file)
                    resultMap.put(file.getName().substring(0, file.getName().lastIndexOf(".")), file.getPath())
                }
            }
        }
        return resultMap
    }
}
