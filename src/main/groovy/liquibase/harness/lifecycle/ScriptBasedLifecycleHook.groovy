package liquibase.harness.lifecycle

import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.exception.LiquibaseException
import liquibase.harness.config.DatabaseUnderTest
import liquibase.Scope

import java.sql.Statement

/**
 * Executes SQL scripts before/after tests based on database type and version.
 * Scripts are loaded from the classpath at predefined locations.
 */
class ScriptBasedLifecycleHook implements TestLifecycleHook {
    
    private static final String INIT_SCRIPT_PATTERN = "liquibase/harness/lifecycle/{database}/{version}/init.sql"
    private static final String CLEANUP_SCRIPT_PATTERN = "liquibase/harness/lifecycle/{database}/{version}/cleanup.sql"
    private static final String TEST_INIT_SCRIPT_PATTERN = "harness/changeObjects/{database}/{testName}.init.sql"
    private static final String TEST_CLEANUP_SCRIPT_PATTERN = "harness/changeObjects/{database}/{testName}.cleanup.sql"
    
    @Override
    void beforeTest(TestContext context) {
        // Execute global init script first
        executeScript(context, INIT_SCRIPT_PATTERN, "pre-test")
        
        // Then execute test-specific init script if it exists
        executeTestScript(context, TEST_INIT_SCRIPT_PATTERN, "test-level init")
    }
    
    @Override
    void afterTest(TestContext context) {
        // Execute test-specific cleanup script first
        executeTestScript(context, TEST_CLEANUP_SCRIPT_PATTERN, "test-level cleanup")
        
        // Then execute global cleanup script
        executeScript(context, CLEANUP_SCRIPT_PATTERN, "post-test")
    }
    
    @Override
    boolean supports(DatabaseUnderTest database) {
        // Check if any lifecycle scripts exist for this database
        return hasLifecycleScripts(database)
    }
    
    private void executeScript(TestContext context, String scriptPattern, String phase) {
        def scriptPaths = resolveScriptPaths(context.database, scriptPattern)
        def resourceAccessor = new ClassLoaderResourceAccessor()
        
        logDebug("Looking for ${phase} scripts at paths: ${scriptPaths}")
        
        for (scriptPath in scriptPaths) {
            try {
                def resources = resourceAccessor.getAll(scriptPath)
                if (resources && !resources.isEmpty()) {
                    logInfo("Executing ${phase} script: ${scriptPath}")
                    
                    def scriptContent = resources.get(0).openInputStream().text
                    logDebug("Script content length: ${scriptContent.length()} characters")
                    executeSQL(context.database, scriptContent)
                    
                    logInfo("Successfully executed ${phase} script: ${scriptPath}")
                    return // Execute only the first found script
                } else {
                    logDebug("No script found at: ${scriptPath}")
                }
            } catch (Exception e) {
                logError("Failed to execute ${phase} script ${scriptPath}", e)
                if (shouldFailOnError()) {
                    throw new LiquibaseException("Lifecycle script execution failed", e)
                }
            }
        }
    }
    
    private void executeTestScript(TestContext context, String scriptPattern, String phase) {
        def scriptPath = scriptPattern
            .replace("{database}", context.database.name.toLowerCase())
            .replace("{testName}", context.testMethodName)
        
        def resourceAccessor = new ClassLoaderResourceAccessor()
        
        logDebug("Looking for ${phase} script at: ${scriptPath}")
        
        try {
            def resources = resourceAccessor.getAll(scriptPath)
            if (resources && !resources.isEmpty()) {
                logInfo("Executing ${phase} script: ${scriptPath}")
                
                def scriptContent = resources.get(0).openInputStream().text
                logDebug("Script content length: ${scriptContent.length()} characters")
                executeSQL(context.database, scriptContent)
                
                logInfo("Successfully executed ${phase} script: ${scriptPath}")
            } else {
                logDebug("No ${phase} script found at: ${scriptPath}")
            }
        } catch (Exception e) {
            logDebug("Failed to execute ${phase} script ${scriptPath}: ${e.message}")
            // Test-level scripts are optional, so we don't throw on error
        }
    }
    
    private void executeSQL(DatabaseUnderTest database, String sql) {
        Statement statement = null
        try {
            // Get the JDBC connection from the Liquibase database object
            def connection = database.database.getConnection()
            if (connection instanceof liquibase.database.jvm.JdbcConnection) {
                statement = connection.getUnderlyingConnection().createStatement()
            } else {
                throw new LiquibaseException("Database connection is not a JDBC connection")
            }
            
            // Split SQL by semicolon but handle it carefully
            def statements = sql.split(";")
            statements.each { sqlStatement ->
                sqlStatement = sqlStatement.trim()
                if (sqlStatement && !sqlStatement.isEmpty()) {
                    logDebug("Executing SQL: ${sqlStatement}")
                    try {
                        statement.execute(sqlStatement)
                        logDebug("SQL executed successfully")
                    } catch (Exception sqlEx) {
                        logError("Failed to execute SQL statement: ${sqlStatement}", sqlEx)
                        throw sqlEx
                    }
                }
            }
        } finally {
            if (statement != null) {
                try {
                    statement.close()
                } catch (Exception e) {
                    // Ignore close errors
                }
            }
        }
    }
    
    private List<String> resolveScriptPaths(DatabaseUnderTest db, String pattern) {
        def paths = []
        
        // Try version-specific path first
        if (db.version) {
            paths.add(pattern
                .replace("{database}", db.name.toLowerCase())
                .replace("{version}", db.version))
        }
        
        // Fall back to database-specific path
        paths.add(pattern
            .replace("{database}", db.name.toLowerCase())
            .replace("/{version}", ""))
        
        return paths
    }
    
    private boolean hasLifecycleScripts(DatabaseUnderTest db) {
        def resourceAccessor = new ClassLoaderResourceAccessor()
        def patterns = [INIT_SCRIPT_PATTERN, CLEANUP_SCRIPT_PATTERN]
        
        for (pattern in patterns) {
            for (path in resolveScriptPaths(db, pattern)) {
                try {
                    def resources = resourceAccessor.getAll(path)
                    if (resources && !resources.isEmpty()) {
                        return true
                    }
                } catch (Exception e) {
                    // Resource not found, continue checking
                }
            }
        }
        return false
    }
    
    private boolean shouldFailOnError() {
        return System.getProperty("liquibase.harness.lifecycle.failOnError", "false") == "true"
    }
    
    private void logInfo(String message) {
        try {
            Scope.getCurrentScope().getUI().sendMessage("[Lifecycle Hook] INFO: " + message)
        } catch (Exception e) {
            println("[Lifecycle Hook] INFO: " + message)
        }
    }
    
    private void logError(String message, Exception e) {
        try {
            Scope.getCurrentScope().getUI().sendErrorMessage("[Lifecycle Hook] ERROR: " + message)
            if (e != null) {
                Scope.getCurrentScope().getUI().sendErrorMessage(e.toString())
            }
        } catch (Exception ex) {
            System.err.println("[Lifecycle Hook] ERROR: " + message)
            if (e != null) {
                e.printStackTrace()
            }
        }
    }
    
    private void logDebug(String message) {
        if (System.getProperty("liquibase.harness.lifecycle.debug", "false") == "true") {
            logInfo("DEBUG: " + message)
        }
    }
}