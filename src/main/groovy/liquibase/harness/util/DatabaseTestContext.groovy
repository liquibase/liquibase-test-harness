package liquibase.harness.util

import liquibase.Scope
import liquibase.database.Database
import liquibase.database.DatabaseConnection
import liquibase.database.DatabaseFactory
import liquibase.database.core.AbstractDb2Database
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.DatabaseException
import liquibase.listener.SqlListener
import liquibase.resource.ClassLoaderResourceAccessor

import java.sql.SQLException

class DatabaseTestContext {
    public static final String ALT_SCHEMA = "LIQUIBASEB"
    private static final String TEST_DATABASES_PROPERTY = "test.databases"
    private static DatabaseTestContext instance = new DatabaseTestContext()
    private Map<String, DatabaseConnection> connectionsByUrl = new HashMap<>()
    private Map<String, Boolean> connectionsAttempted = new HashMap<>()

    static DatabaseTestContext getInstance() {
        return instance
    }

    /**
     * Makes a best effort to gracefully shut down a (possible open) databaseConnection and ignores any
     * errors that happen during that process.
     *
     * @param databaseConnection
     */
    private static void shutdownConnection(DatabaseConnection databaseConnection) {
        try {
            if (databaseConnection instanceof JdbcConnection) {
                try {
                    if (!databaseConnection.getUnderlyingConnection().getAutoCommit()) {
                        databaseConnection.getUnderlyingConnection().rollback()
                    }
                } catch (SQLException e) {
                    // Ignore. If rollback fails or is impossible, there is nothing we can do about it.
                }

                // Close the JDBC connection
                databaseConnection.getUnderlyingConnection().close()

            } else {
                databaseConnection.close()
            }
        } catch (SQLException | DatabaseException e) {
            Scope.getCurrentScope().getLog(DatabaseTestContext.class).warning("Could not close the following connection: "
                    + databaseConnection.getURL(), e)
        }
    }


/**
 * Returns a DatabaseConnection for a givenUrl is one is already open. If not, attempts to create it, but only
 * if a previous attempt at creating the connection has NOT failed (to prevent unnecessary connection attempts
 * during the integration tests).
 *
 * @param givenUrl The JDBC URL to connect to
 * @param username the user name to use to log in to the instance (may be null, esp. for embedded DBMS)
 * @param password the password for the username (may be null)
 * @return a DatabaseConnection if one has been established or fetched from the cache successfully, null otherwise
 * @throws Exception if an error occurs while trying to get the connection
 */
    private DatabaseConnection openConnection(final String givenUrl,
                                              final String username, final String password) throws Exception {
        // Insert the temp dir path and ensure our replacement ends with /
        String tempDir = System.getProperty("java.io.tmpdir")
        if (!tempDir.endsWith(System.getProperty("file.separator")))
            tempDir += System.getProperty("file.separator")

        final String url = givenUrl.replace("***TEMPDIR***/", tempDir)

        if (connectionsAttempted.containsKey(url)) {
            def connection = connectionsByUrl.get(url)
            if (connection == null) {
                return null
            }

            if (connection instanceof JdbcConnection && connection.getUnderlyingConnection().isClosed()) {
                connection = DatabaseFactory.getInstance().openConnection(url, username, password,
                        null, new ClassLoaderResourceAccessor())
                connectionsByUrl.put(url, connection)
            }
            return connection
        }

        connectionsAttempted.put(url, Boolean.TRUE)

        if (System.getProperty(TEST_DATABASES_PROPERTY) != null) {
            boolean shouldTest = false
            String[] databasesToTest = System.getProperty(TEST_DATABASES_PROPERTY).split("\\s*,\\s*")
            for (String database : databasesToTest) {
                if (url.contains(database)) {
                    shouldTest = true
                }
            }
            if (!shouldTest) {
                System.out.println("test.databases system property forbids testing against " + url)
                return null
            } else {
                System.out.println("Will be tested against " + url)
            }
        }

        DatabaseConnection connection = DatabaseFactory.getInstance().openConnection(url, username, password,
                null, new ClassLoaderResourceAccessor())
        if (connection == null) {
            return null
        }

        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection)
        final DatabaseConnection databaseConnection = database.getConnection()

        if (databaseConnection.getAutoCommit()) {
            databaseConnection.setAutoCommit(false)
        }

        try {
            if (url.startsWith("jdbc:hsql")) {
                String sql = "CREATE SCHEMA " + ALT_SCHEMA + " AUTHORIZATION DBA"
                for (SqlListener listener : Scope.getCurrentScope().getListeners(SqlListener.class)) {
                    listener.writeSqlWillRun(sql)
                }
                ((JdbcConnection) databaseConnection).getUnderlyingConnection().createStatement().execute(sql)
            } else if (url.startsWith("jdbc:sqlserver")
                    || url.startsWith("jdbc:postgresql")
                    || url.startsWith("jdbc:h2")) {
                String sql = "CREATE SCHEMA " + ALT_SCHEMA
                for (SqlListener listener : Scope.getCurrentScope().getListeners(SqlListener.class)) {
                    listener.writeSqlWillRun(sql)
                }
                ((JdbcConnection) databaseConnection).getUnderlyingConnection().createStatement().execute(sql)
            }
            if (!databaseConnection.getAutoCommit()) {
                databaseConnection.commit()
            }
        } catch (SQLException e) {
            // schema already exists
        } finally {
            try {
                databaseConnection.rollback()
            } catch (DatabaseException e) {
                if (database instanceof AbstractDb2Database) {
//                    expected, there is a problem with it
                } else {
                    throw e
                }
            }
        }

        connectionsByUrl.put(url, databaseConnection)

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            void run() {
                shutdownConnection(databaseConnection)
            }
        }))
        return databaseConnection
    }

    DatabaseConnection getConnection(String url, String username, String password) throws Exception {
        return openConnection(url, username, password)
    }
}
