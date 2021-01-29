package liquibase.harness.config

import groovy.transform.ToString
import liquibase.database.DatabaseFactory
import liquibase.database.OfflineConnection
import liquibase.lockservice.LockServiceFactory
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.ResourceAccessor
import liquibase.harness.util.DatabaseConnectionUtil
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
    private boolean databasesConnected = false

    TestConfig() {
    }

    static TestConfig getInstance() {
        if (instance == null) {
            Yaml configFileYml = new Yaml()
            def testConfig = getClass().getResourceAsStream("/harness-config.yml")
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

    public List<DatabaseUnderTest> getDatabasesUnderTest() {
        if (!databasesConnected) {
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

            for (def databaseUnderTest : this.databasesUnderTest) {
                def initThread = new Thread({
                    databaseUnderTest.database = DatabaseConnectionUtil.initializeDatabase(databaseUnderTest.url, databaseUnderTest.username, databaseUnderTest.password)
                    if (databaseUnderTest.database == null) {
                        Logger.getLogger(TestConfig.name).severe("Cannot connect to $databaseUnderTest.url. Using offline" +
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
                        Logger.getLogger(TestConfig.name)
                                .warning("Database version is not provided applying version from Database metadata")
                        Integer minorVersion = databaseUnderTest.database.getDatabaseMinorVersion()
                        databaseUnderTest.version = databaseUnderTest.database.getDatabaseMajorVersion().toString().concat(
                                minorVersion ? "." + minorVersion : "")
                    } else if (databaseUnderTest.name != databaseUnderTest.database.shortName ||
                            !databaseUnderTest.version.startsWith(databaseUnderTest.database.databaseMajorVersion.toString())) {
                        Logger.getLogger(TestConfig.name).severe("Provided database name/majorVersion doesn't match with actual\
            ${System.getProperty("line.separator")}    provided: ${databaseUnderTest.name} ${databaseUnderTest.version}\
            ${System.getProperty("line.separator")}    actual: ${databaseUnderTest.database.shortName} \
            ${databaseUnderTest.database.databaseMajorVersion.toString()}")
                    }
                })
                initThread.start()
                initThread.join()
            }
            databasesConnected = true
        }
        return databasesUnderTest

    }

}