package liquibase.sdk.test.util


import liquibase.sdk.test.config.TestConfig
import liquibase.sdk.test.config.TestInput
import liquibase.util.StreamUtil
import org.yaml.snakeyaml.Yaml

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
}
