package liquibase.harness.util


import liquibase.harness.config.TestConfig
import liquibase.util.StreamUtil

class FileUtils {

    static String getFileContent(String change, String databaseName, String version , String expectedFolder,
                                 String fileExtension) {
        def resourceAccessor = TestConfig.instance.resourceAccessor

        def content = resourceAccessor.openStream(null, expectedFolder + "/" + databaseName + "/" + version + "/" + change + fileExtension)
        if (content != null) {
            return StreamUtil.readStreamAsString(content)
        }

        content = resourceAccessor.openStream(null, expectedFolder + "/" + databaseName + "/" + change + fileExtension)
        if (content != null) {
            return StreamUtil.readStreamAsString(content)
        }

        content = resourceAccessor.openStream(null, expectedFolder + "/" + change + fileExtension)
        if (content != null) {
            return StreamUtil.readStreamAsString(content)
        }

        return null
    }

    static String getExpectedSqlFileContent(String changeObject, String databaseName,
                                            String version, String expectedFolder) {
        return getFileContent(changeObject, databaseName, version, expectedFolder, ".sql")
    }

    static String getExpectedJSONFileContent(String changeObject, String databaseName,
                                             String version, String expectedFolder) {
        return getFileContent(changeObject, databaseName, version, expectedFolder, ".json")
    }
}