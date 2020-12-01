package liquibase.sdk.test.config

import groovy.transform.ToString
import liquibase.Scope
import liquibase.database.DatabaseFactory
import liquibase.database.OfflineConnection
import liquibase.lockservice.LockServiceFactory
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.ResourceAccessor
import liquibase.sdk.test.util.DatabaseConnectionUtil
import liquibase.sdk.test.util.TestUtils
import org.yaml.snakeyaml.Yaml

import java.util.logging.Logger
import java.util.stream.Collectors;

@ToString
class TestConfig {

    private static TestConfig instance

    String outputResourcesBase = "src/test/resources"
    ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor()
    Boolean revalidateSql
    String inputFormat
    String context
    List<DatabaseUnderTest> databasesUnderTest

    TestConfig() {
    }

    public static TestConfig getInstance() {
        if (instance == null) {
            Yaml configFileYml = new Yaml()
            def testConfig = getClass().getResourceAsStream("/liquibase.sdk.test.yml")
            assert testConfig != null : "Cannot find liquibase.sdk.test.yml in classpath"

            instance = configFileYml.loadAs(testConfig, TestConfig.class)

            if (System.getProperty("revalidateSql") == null) {
                instance.revalidateSql = true
            } else {
                instance.revalidateSql = Boolean.valueOf(System.getProperty("revalidateSql"))
            }
            Logger.getLogger(this.class.name).info("Revalidate SQL: ${instance.revalidateSql}")

            String dbName = System.getProperty("dbName")
            String dbVersion = System.getProperty("dbVersion")

            if (dbName) {
                instance.databasesUnderTest = instance.databasesUnderTest.stream()
                        .filter({ it.name.equalsIgnoreCase(dbName) })
                        .collect(Collectors.toList())
            }

            if (dbVersion) {
                instance.databasesUnderTest = instance.databasesUnderTest.stream()
                        .filter({ it.version.equalsIgnoreCase(dbVersion) })
                        .collect(Collectors.toList())
            }

            for (def databaseUnderTest : instance.databasesUnderTest) {
                databaseUnderTest.database = DatabaseConnectionUtil.initializeDatabase(databaseUnderTest.url, databaseUnderTest.username, databaseUnderTest.password)
                if (databaseUnderTest.database == null) {
                    Logger.getLogger(this.class.name).severe("Cannot connect to $databaseUnderTest.url. Using offline" +
                            " connection")

                    for (def possibleDatabase : DatabaseFactory.getInstance().getImplementedDatabases()) {
                        if (possibleDatabase.getDefaultDriver(databaseUnderTest.url) != null) {
                            println "Database ${possibleDatabase.shortName} accepts $databaseUnderTest.url"

                            databaseUnderTest.database = DatabaseConnectionUtil.initializeDatabase("offline:${possibleDatabase.shortName}", databaseUnderTest.username, null)
                            break
                        }
                    }
                } else {
                    LockServiceFactory.getInstance().getLockService(databaseUnderTest.database).forceReleaseLock()
                }

                databaseUnderTest.database.outputDefaultCatalog = false
                databaseUnderTest.database.outputDefaultSchema = false

                if (databaseUnderTest.name == null) {
                    databaseUnderTest.name = databaseUnderTest.database.getShortName()
                    if (databaseUnderTest.database.connection instanceof OfflineConnection) {
                        databaseUnderTest.name += " ${databaseUnderTest.url}"
                    } else {
                        databaseUnderTest.name += " ${databaseUnderTest.database.getDatabaseProductVersion()}"
                    }
                } else if (databaseUnderTest.version == null) {
                    Logger.getLogger(this.class.name)
                            .warning("Database version is not provided applying version from Database metadata")
                    databaseUnderTest.version = databaseUnderTest.database.getDatabaseMajorVersion() + "."
                    +databaseUnderTest.database.getDatabaseMinorVersion()
                }
                else if (databaseUnderTest.name != databaseUnderTest.database.shortName ||
                        !databaseUnderTest.version.startsWith(databaseUnderTest.database.databaseMajorVersion.toString())) {
                    Logger.getLogger(this.class.name).severe("Provided database name/majorVersion doesn't match with actual\
${System.getProperty("line.separator")}    provided: ${databaseUnderTest.name} ${databaseUnderTest.version}\
${System.getProperty("line.separator")}    actual: ${databaseUnderTest.database.shortName} \
${databaseUnderTest.database.databaseMajorVersion.toString()}")
                }
            }
        }
        return instance

    }
}
