package liquibase.harness.util


import liquibase.harness.config.TestConfig
import liquibase.util.StreamUtil

class FileUtils {

    static String getFileContent(String changeObject, String databaseName, String version , String expectedFolder,
                                 String fileExtension) {
        def resourceAccessor = TestConfig.instance.resourceAccessor

        def content = resourceAccessor.openStream(null, expectedFolder + "/" + databaseName + "/" + version + "/" + changeObject + fileExtension)
        if (content != null) {
            return StreamUtil.readStreamAsString(content)
        }

        content = resourceAccessor.openStream(null, expectedFolder + "/" + databaseName + "/" + changeObject + fileExtension)
        if (content != null) {
            return StreamUtil.readStreamAsString(content)
        }

        content = resourceAccessor.openStream(null, expectedFolder + "/" + changeObject + fileExtension)
        if (content != null) {
            return StreamUtil.readStreamAsString(content)
        }

        return null
    }

    static String getExpectedSqlFileContent(String changeObject, String databaseName, String version) {
        return getFileContent(changeObject, databaseName, version, "liquibase/harness/change/expectedSql", ".sql")
    }

    static String getExpectedSnapshotFileContent(String changeObject, String databaseName, String version) {
        return getFileContent(changeObject, databaseName, version, "liquibase/harness/change/expectedSnapshot", ".json")
    }
}