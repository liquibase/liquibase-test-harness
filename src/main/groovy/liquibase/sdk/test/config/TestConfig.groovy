package liquibase.sdk.test.config

import groovy.transform.ToString
import liquibase.database.DatabaseFactory
import liquibase.database.OfflineConnection
import liquibase.lockservice.LockServiceFactory
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.ResourceAccessor
import liquibase.sdk.test.util.DatabaseConnectionUtil
import liquibase.sdk.test.util.TestUtils
import org.yaml.snakeyaml.Yaml

import java.util.logging.Logger;

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
            instance = configFileYml.loadAs(getClass().getResourceAsStream("/liquibase.sdk.test.yml"), TestConfig.class)

            if (System.getProperty("revalidateSql") == null) {
                instance.revalidateSql = true
            } else {
                instance.revalidateSql = Boolean.valueOf(System.getProperty("revalidateSql"))
            }
            Logger.getLogger(TestConfig.name).info("Revalidate SQL: ${instance.revalidateSql}")

            String dbName = System.getProperty("dbName")
            String dbVersion = System.getProperty("dbVersion")

            if (dbName) {
                //TODO try improve this, add logging
                instance.databasesUnderTest = instance.databasesUnderTest.stream()
                        .filter({ it.name.equalsIgnoreCase(dbName) })
                        .findAny()
                        .map({ Collections.singletonList(it) })
                        .orElse(instance.databasesUnderTest)

                if (dbVersion)
                    for (DatabaseUnderTest databaseUnderTest : instance.databasesUnderTest) {
                        databaseUnderTest.versions = databaseUnderTest.versions.stream()
                                .filter({ it.version.equalsIgnoreCase(dbVersion) })
                                .findAny()
                                .map({ Collections.singletonList(it) })
                                .orElse(databaseUnderTest.versions)
                    }
            }

            for (def databaseUnderTest : instance.databasesUnderTest) {
                databaseUnderTest.database = DatabaseConnectionUtil.initializeDatabase(databaseUnderTest.url, databaseUnderTest.username, databaseUnderTest.password)
                if (databaseUnderTest.database == null) {
                    Logger.getLogger(TestUtils.name).severe("Cannot connect to $databaseUnderTest.url. Using offline connection")

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
                }
            }
        }

        return instance

    }
}
