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
    Boolean initDB = true
    ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor()
    Boolean revalidateSql = true
    String inputFormat
    String context
    List<DatabaseUnderTest> databasesUnderTest
    Boolean databasesConnected = false

    TestConfig() {
    }

    static TestConfig getInstance() {
        if (instance == null) {
            Yaml configFileYml = new Yaml()
            String testConfigPath = System.getProperty("configFile") ?: "/harness-config.yml"
            def testConfig = TestConfig.class.getResourceAsStream(testConfigPath)
            assert testConfig != null: "Cannot find harness-config.yml in classpath"

            instance = configFileYml.loadAs(testConfig, TestConfig.class)

            if (System.getProperty("revalidateSql") != null) {
                instance.revalidateSql = Boolean.valueOf(System.getProperty("revalidateSql"))
            }
            Logger.getLogger(TestConfig.name).info("Revalidate SQL: ${instance.revalidateSql}")
        }
        return instance
    }

    List<DatabaseUnderTest> getDatabasesUnderTest() {
        String dbName = System.getProperty("dbName")
        String dbVersion = System.getProperty("dbVersion")
        String platformPrefix = System.getProperty("prefix")
        Logger.getLogger(this.class.name).info("DB name = " + dbName + " DB version = " + dbVersion + " Prefix = " + platformPrefix + " Databases under test " + databasesUnderTest.toString())
        if (platformPrefix) {
            this.databasesUnderTest = this.databasesUnderTest.stream()
                    .filter({ platformPrefix.equalsIgnoreCase(it.prefix) })
                    .collect(Collectors.toList())
        }
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

    /**
     * Replace dash for major/minor version separator for AWS test runs
     */
    private static String adjustVersionSeparator(String version) {
        return version.replaceAll("-", ".")
    }
}
