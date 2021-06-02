package liquibase.harness.util

import liquibase.Scope
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.database.Database
import liquibase.database.DatabaseConnection
import liquibase.database.DatabaseFactory
import liquibase.database.OfflineConnection
import liquibase.exception.DatabaseException
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.lockservice.LockServiceFactory
import liquibase.logging.Logger
import liquibase.snapshot.SnapshotGeneratorFactory

class DatabaseConnectionUtil {
    private static Logger logger = Scope.getCurrentScope().getLog(getClass())

    static Database initializeDatabase(String url, String username, String password) {
        try {
            Database database = openConnection(url, username, password)
            if (database == null) {
                return null
            }
            return TestConfig.instance.initDB ? init(database) : database
        }
        catch (Exception e) {
            logger.severe("Unable to initialize database connection: ${e.getMessage()}", e)
            return null
        }
    }

    List<DatabaseUnderTest> initializeDatabasesConnection(List<DatabaseUnderTest> databasesUnderTests) {
        if (!TestConfig.instance.databasesConnected) {
            for (def databaseUnderTest : databasesUnderTests) {
                def initThread = new Thread({
                    databaseUnderTest.database = initializeDatabase(databaseUnderTest.url, databaseUnderTest.username, databaseUnderTest.password)
                    if (databaseUnderTest.database == null) {
                        java.util.logging.Logger.getLogger(TestConfig.name).severe("Cannot connect to $databaseUnderTest.url. Using offline" +
                                " connection")

                        for (def possibleDatabase : DatabaseFactory.getInstance().getImplementedDatabases()) {
                            if (possibleDatabase.getDefaultDriver(databaseUnderTest.url) != null) {
                                println "Database ${possibleDatabase.shortName} accepts $databaseUnderTest.url"

                                databaseUnderTest.database = initializeDatabase("offline:${possibleDatabase.shortName}", databaseUnderTest.username, null)
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
                        java.util.logging.Logger.getLogger(TestConfig.name)
                                .warning("Database version is not provided applying version from Database metadata")
                        Integer minorVersion = databaseUnderTest.database.getDatabaseMinorVersion()
                        databaseUnderTest.version = databaseUnderTest.database.getDatabaseMajorVersion().toString().concat(
                                minorVersion ? "." + minorVersion : "")
                    } else if (databaseUnderTest.name != databaseUnderTest.database.shortName ||
                            !databaseUnderTest.version.startsWith(databaseUnderTest.database.databaseMajorVersion.toString())) {
                        java.util.logging.Logger.getLogger(TestConfig.name).severe("Provided database name/majorVersion doesn't match with actual\
        ${System.getProperty("line.separator")}    provided: ${databaseUnderTest.name} ${databaseUnderTest.version}\
        ${System.getProperty("line.separator")}    actual: ${databaseUnderTest.database.shortName} \
        ${databaseUnderTest.database.databaseMajorVersion.toString()}")
                    }
                })
                initThread.start()
                initThread.join()
            }
        }
        TestConfig.instance.databasesConnected = true

        return databasesUnderTests
    }

    private static Database openConnection(String url, String username, String password) throws Exception {
        DatabaseConnection connection = DatabaseTestContext.getInstance().getConnection(url, username, password)
        if (connection == null) {
            return null
        }
        return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection)

    }

    private static Database init(Database database) throws DatabaseException {
        SnapshotGeneratorFactory.resetAll()
        LockServiceFactory.getInstance().resetAll()
        LockServiceFactory.getInstance().getLockService(database).init()
        ChangeLogHistoryServiceFactory.getInstance().resetAll()
        return database
    }

}
