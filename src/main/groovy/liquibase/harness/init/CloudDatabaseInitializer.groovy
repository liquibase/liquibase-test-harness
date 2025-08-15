package liquibase.harness.init

import liquibase.Liquibase
import liquibase.Contexts
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.harness.config.DatabaseUnderTest
import liquibase.Scope
import liquibase.exception.LiquibaseException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles one-time initialization of cloud databases.
 * This is a singleton to ensure initialization happens only once per JVM.
 */
class CloudDatabaseInitializer {
    
    private static final CloudDatabaseInitializer INSTANCE = new CloudDatabaseInitializer()
    
    protected CloudDatabaseInitializer() {
        // Protected constructor for testing
    }
    
    // Track which databases have been initialized
    private final Set<String> initializedDatabases = ConcurrentHashMap.newKeySet()
    
    // Cloud provider URL patterns
    private static final List<String> CLOUD_PATTERNS = [
        'rds.amazonaws.com',           // AWS RDS
        'cluster-.*.amazonaws.com',    // AWS Aurora
        'database.windows.net',        // Azure Database
        'database.azure.com',          // Azure Database (newer)
        'googleapis.com',              // GCP Cloud SQL
        'oraclecloud.com',            // Oracle Cloud
        'db.aiven.io',                // Aiven managed databases
        'snowflakecomputing.com'      // Snowflake
    ]
    
    static CloudDatabaseInitializer getInstance() {
        return INSTANCE
    }
    
    /**
     * Initialize database if not already initialized
     * @param database The database configuration
     * @return true if initialization was performed, false if already initialized
     */
    boolean initializeIfNeeded(DatabaseUnderTest database) {
        def databaseKey = generateDatabaseKey(database)
        
        // Check if already initialized
        if (initializedDatabases.contains(databaseKey)) {
            logDebug("Database ${databaseKey} already initialized, skipping")
            return false
        }
        
        // Skip if no init configuration
        if (!database.initScript && !database.initChangelog) {
            logDebug("No init script or changelog configured for ${database.name}")
            return false
        }
        
        // Skip if explicitly disabled
        if (database.skipInit) {
            logInfo("Skipping init for ${database.name} (skipInit=true)")
            return false
        }
        
        // Perform initialization
        synchronized (this) {
            // Double-check after acquiring lock
            if (initializedDatabases.contains(databaseKey)) {
                return false
            }
            
            try {
                logInfo("Initializing cloud database: ${databaseKey}")
                
                if (database.initScript) {
                    executeInitScript(database)
                } else if (database.initChangelog) {
                    executeInitChangelog(database)
                }
                
                // Mark as initialized
                initializedDatabases.add(databaseKey)
                logInfo("Successfully initialized cloud database: ${databaseKey}")
                return true
                
            } catch (Exception e) {
                logError("Failed to initialize database ${databaseKey}: ${e.message}", e)
                
                if (shouldFailOnInitError()) {
                    throw new RuntimeException("Cloud database initialization failed for ${databaseKey}", e)
                }
                return false
            }
        }
    }
    
    protected void executeInitScript(DatabaseUnderTest database) {
        def scriptPath = resolveScriptPath(database.initScript, database)
        def resourceAccessor = new ClassLoaderResourceAccessor()
        
        // Check if script exists
        def resources = resourceAccessor.getAll(scriptPath)
        if (!resources || resources.isEmpty()) {
            throw new FileNotFoundException("Init script not found: ${scriptPath}")
        }
        
        logInfo("Executing init script: ${scriptPath}")
        
        // Read script content
        def scriptContent = resources.get(0).openInputStream().text
        
        // For SQL scripts, execute directly using JDBC
        if (scriptPath.endsWith('.sql')) {
            executeSQLScript(database, scriptContent)
        } else {
            // For other formats, use Liquibase
            executeLiquibaseScript(database, scriptPath)
        }
        
        logInfo("Init script completed successfully")
    }
    
    protected void executeSQLScript(DatabaseUnderTest database, String sqlContent) {
        Connection connection = null
        Statement statement = null
        
        try {
            connection = createConnection(database)
            statement = connection.createStatement()
            
            // Split SQL by semicolon but handle carefully
            // For Snowflake, we need to handle multi-statement execution
            def statements = sqlContent.split(';')
            statements.each { sqlStatement ->
                sqlStatement = sqlStatement.trim()
                
                // Remove comment lines and empty lines
                def lines = sqlStatement.split('\n')
                def cleanLines = lines.findAll { line ->
                    def trimmed = line.trim()
                    return trimmed && !trimmed.startsWith('--')
                }
                def cleanStatement = cleanLines.join(' ').trim()
                
                if (cleanStatement && !cleanStatement.isEmpty()) {
                    logDebug("Executing SQL: ${cleanStatement.take(100)}...")
                    try {
                        statement.execute(cleanStatement)
                    } catch (Exception e) {
                        // Log but continue - some statements might fail safely
                        logWarn("SQL statement failed (continuing): ${e.message}")
                        if (!shouldContinueOnSQLError()) {
                            throw e
                        }
                    }
                }
            }
        } finally {
            if (statement != null) {
                try { statement.close() } catch (Exception e) { /* ignore */ }
            }
            if (connection != null) {
                try { connection.close() } catch (Exception e) { /* ignore */ }
            }
        }
    }
    
    private void executeLiquibaseScript(DatabaseUnderTest database, String scriptPath) {
        def connection = createConnection(database)
        try {
            def jdbcConnection = new JdbcConnection(connection)
            def liquibaseDb = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(jdbcConnection)
            
            def resourceAccessor = new ClassLoaderResourceAccessor()
            def liquibase = new Liquibase(scriptPath, resourceAccessor, liquibaseDb)
            liquibase.update("")
        } finally {
            connection.close()
        }
    }
    
    private void executeInitChangelog(DatabaseUnderTest database) {
        def changelogPath = resolveScriptPath(database.initChangelog, database)
        def resourceAccessor = new ClassLoaderResourceAccessor()
        
        // Check if changelog exists
        def resources = resourceAccessor.getAll(changelogPath)
        if (!resources || resources.isEmpty()) {
            throw new FileNotFoundException("Init changelog not found: ${changelogPath}")
        }
        
        logInfo("Executing init changelog: ${changelogPath}")
        
        def connection = createConnection(database)
        try {
            def jdbcConnection = new JdbcConnection(connection)
            def liquibaseDb = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(jdbcConnection)
            
            def liquibase = new Liquibase(changelogPath, resourceAccessor, liquibaseDb)
            
            // Use specific context for cloud init
            liquibase.update(new Contexts("cloud-init"))
            
            logInfo("Init changelog completed successfully")
            
        } finally {
            connection.close()
        }
    }
    
    protected Connection createConnection(DatabaseUnderTest database) {
        // Apply any init properties
        def props = new Properties()
        if (database.initProperties) {
            database.initProperties.each { key, value ->
                props.setProperty(key, value)
            }
        }
        if (database.username) {
            props.setProperty("user", database.username)
        }
        if (database.password) {
            props.setProperty("password", database.password)
        }
        
        return DriverManager.getConnection(database.url, props)
    }
    
    private String resolveScriptPath(String pathTemplate, DatabaseUnderTest database) {
        if (!pathTemplate) return null
        
        return pathTemplate
            .replace("{database}", database.name.toLowerCase())
            .replace("{version}", database.version ?: "")
            .replace("//", "/") // Clean up double slashes
    }
    
    private String generateDatabaseKey(DatabaseUnderTest database) {
        // Create unique key based on URL and database name
        // This ensures different configurations of same database are handled separately
        return "${database.url}::${database.name}::${database.version ?: 'default'}"
    }
    
    /**
     * Check if this is a cloud database based on URL patterns
     */
    static boolean isCloudDatabase(DatabaseUnderTest database) {
        if (!database.url) return false
        
        return CLOUD_PATTERNS.any { pattern ->
            database.url.matches(".*${pattern}.*")
        }
    }
    
    protected boolean shouldFailOnInitError() {
        return System.getProperty("liquibase.harness.cloud.init.failOnError", "false") == "true"
    }
    
    protected boolean shouldContinueOnSQLError() {
        return System.getProperty("liquibase.harness.cloud.init.continueOnSqlError", "true") == "true"
    }
    
    /**
     * Reset initialization state (useful for testing)
     */
    void reset() {
        initializedDatabases.clear()
    }
    
    // Logging methods
    protected void logInfo(String message) {
        try {
            Scope.getCurrentScope().getUI().sendMessage("[Cloud Init] INFO: " + message)
        } catch (Exception e) {
            println("[Cloud Init] INFO: " + message)
        }
    }
    
    protected void logWarn(String message) {
        try {
            Scope.getCurrentScope().getUI().sendMessage("[Cloud Init] WARN: " + message)
        } catch (Exception e) {
            println("[Cloud Init] WARN: " + message)
        }
    }
    
    protected void logError(String message, Exception e) {
        try {
            Scope.getCurrentScope().getUI().sendErrorMessage("[Cloud Init] ERROR: " + message)
            if (e && isDebugEnabled()) {
                Scope.getCurrentScope().getUI().sendErrorMessage(e.toString())
            }
        } catch (Exception ex) {
            System.err.println("[Cloud Init] ERROR: " + message)
            if (e && isDebugEnabled()) {
                e.printStackTrace()
            }
        }
    }
    
    protected void logDebug(String message) {
        if (isDebugEnabled()) {
            logInfo("DEBUG: " + message)
        }
    }
    
    protected boolean isDebugEnabled() {
        return System.getProperty("liquibase.harness.cloud.init.debug", "false") == "true"
    }
}