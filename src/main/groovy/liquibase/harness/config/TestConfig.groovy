package liquibase.harness.config

import groovy.transform.ToString
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.ResourceAccessor
import org.yaml.snakeyaml.Yaml

import java.util.logging.Logger
import java.util.stream.Collectors


@ToString
class TestConfig {

    private static TestConfig instance

    String outputResourcesBase = "src/test/resources"
    boolean initDB = true
    ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor()
    Boolean revalidateSql
    String inputFormat
    String context
    List<DatabaseUnderTest> databasesUnderTest
    boolean databasesConnected = false

    TestConfig() {
    }

    static TestConfig getInstance() {
        if (instance == null) {
            Yaml configFileYml = new Yaml()
            def testConfig = TestConfig.class.getResourceAsStream("/harness-config.yml")
            assert testConfig != null : "Cannot find harness-config.yml in classpath"

            instance = configFileYml.loadAs(testConfig, TestConfig.class)

            if (System.getProperty("revalidateSql") == null) {
                instance.revalidateSql = true
            } else {
                instance.revalidateSql = Boolean.valueOf(System.getProperty("revalidateSql"))
            }
            Logger.getLogger(TestConfig.name).info("Revalidate SQL: ${instance.revalidateSql}")
        }

        return instance
    }

    List<DatabaseUnderTest> getDatabasesUnderTest() {
        String dbName = System.getProperty("dbName")
        String dbVersion = System.getProperty("dbVersion")

        if (dbName) {
            this.databasesUnderTest = this.databasesUnderTest.stream()
                    .filter({ it.name.equalsIgnoreCase(dbName) })
                    .collect(Collectors.toList())
        }

        if (dbVersion) {
            this.databasesUnderTest = this.databasesUnderTest.stream()
                    .filter({ it.version.equalsIgnoreCase(dbVersion) })
                    .collect(Collectors.toList())
        }

        return databasesUnderTest
    }
}
