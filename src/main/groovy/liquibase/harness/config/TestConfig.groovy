package liquibase.harness.config

import groovy.transform.ToString
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.ResourceAccessor
import org.yaml.snakeyaml.Yaml
import java.util.logging.Logger

@ToString
class TestConfig {

    private static TestConfig instance
    String outputResourcesBase = "src/test/resources"
    Boolean initDB = true
    ResourceAccessor resourceAccessor = new FirstMatchResourceAccessor(new ClassLoaderResourceAccessor())
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

    List<DatabaseUnderTest> getFilteredDatabasesUnderTest() {
        String dbName = System.getProperty("dbName")
        String dbVersion = System.getProperty("dbVersion")?.replaceAll("-", ".")
        String platformPrefix = System.getProperty("prefix")
        String dbUsername = System.getProperty("dbUsername")
        String dbPassword = System.getProperty("dbPassword")
        String dbUrl = System.getProperty("dbUrl")
        databasesUnderTest.forEach({ it -> it.version = it.version?.replaceAll("-", ".") })

        if (platformPrefix) {
            databasesUnderTest = databasesUnderTest.findAll { platformPrefix.equalsIgnoreCase(it.prefix)}
        }
        if (dbName) {
            databasesUnderTest = databasesUnderTest.findAll { it.name.equalsIgnoreCase(dbName)}
        }

        if (dbVersion) {
            databasesUnderTest.forEach( {it.version = it.version.equalsIgnoreCase("DBVERSION") ? dbVersion : it.version })
            databasesUnderTest = databasesUnderTest.findAll {it.version.equalsIgnoreCase(dbVersion)}
        }

        if(dbUsername) {
            databasesUnderTest.forEach( {it.username = dbUsername})
        }

        if(dbPassword) {
            databasesUnderTest.forEach( {it.password = dbPassword})
        }

        if(dbUrl) {
            databasesUnderTest.forEach( {it.url = dbUrl})
        }

        return databasesUnderTest
    }

}
