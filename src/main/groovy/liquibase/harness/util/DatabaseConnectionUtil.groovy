package liquibase.harness.util

import liquibase.Scope
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.database.Database
import liquibase.database.DatabaseConnection
import liquibase.database.DatabaseFactory
import liquibase.exception.DatabaseException
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

            return init(database)
        }
        catch (Exception e) {
            logger.severe("Unable to initialize database connection: ${e.getMessage()}", e)
            return null
        }
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
