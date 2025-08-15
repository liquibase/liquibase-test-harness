package liquibase.harness.lifecycle

import liquibase.harness.config.DatabaseUnderTest
import java.sql.Statement

/**
 * Minimal schema isolation hook for Snowflake tests.
 * Creates unique isolated schemas for each test.
 */
class SchemaIsolationHook implements TestLifecycleHook {
    
    @Override
    void beforeTest(TestContext context) {
        if (!isSnowflake(context.database)) {
            return
        }
        
        // Create a predictable schema name based on test name
        def testName = sanitizeTestName(context.testMethodName)
        def schemaName = "TEST_${testName}".toUpperCase()
        
        println("SchemaIsolationHook: Setting up isolated schema: ${schemaName}")
        
        try {
            // Create the test schema
            setupSchema(context.database, schemaName)
            
            // Update database to use the isolated schema
            def database = context.database.database
            if (database.respondsTo('setDefaultSchemaName')) {
                println("SchemaIsolationHook: Setting default schema to ${schemaName}")  
                database.setDefaultSchemaName(schemaName)
                println("SchemaIsolationHook: New default schema: ${database.getDefaultSchemaName()}")
            }
            
            // Also switch the current connection to use the new schema
            def connection = context.database.database.getConnection()?.getUnderlyingConnection()
            if (connection) {
                def statement = connection.createStatement()
                println("SchemaIsolationHook: Executing USE WAREHOUSE LTHDB_TEST_WH")
                statement.execute("USE WAREHOUSE LTHDB_TEST_WH")
                println("SchemaIsolationHook: Executing USE SCHEMA ${schemaName}")
                statement.execute("USE SCHEMA ${schemaName}")
                statement.close()
                println("SchemaIsolationHook: Current connection now using schema ${schemaName}")
            }
            
            // Store for cleanup
            context.addMetadata("testSchema", schemaName)
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up isolated schema: ${schemaName}", e)
        }
    }
    
    @Override
    void afterTest(TestContext context) {
        if (!isSnowflake(context.database)) {
            return
        }
        
        def schemaName = context.getMetadata("testSchema")
        if (!schemaName) {
            return
        }
        
        println("SchemaIsolationHook: Cleaning up schema ${schemaName}")
        try {
            // Clean up the test schema
            cleanupSchema(context.database, schemaName)
        } catch (Exception e) {
            // Log but don't fail - cleanup is best effort
            System.err.println("Warning: Failed to clean up test schema ${schemaName}: ${e.message}")
        }
    }
    
    @Override
    boolean supports(DatabaseUnderTest database) {
        return isSnowflake(database) && database.useSchemaIsolation
    }
    
    private boolean isSnowflake(DatabaseUnderTest database) {
        return database.name?.toLowerCase()?.contains("snowflake") ?: false
    }
    
    private String sanitizeTestName(String testName) {
        if (!testName) {
            return "UNKNOWN"
        }
        
        // Replace non-alphanumeric characters with underscores
        String sanitized = testName.replaceAll(/[^A-Za-z0-9_]/, "_").toUpperCase()
        
        // Ensure it starts with a letter or underscore (SQL identifier rules)
        if (sanitized && sanitized[0].matches(/[0-9]/)) {
            sanitized = "_" + sanitized
        }
        
        return sanitized ?: "UNKNOWN"
    }
    
    private void setupSchema(DatabaseUnderTest database, String schemaName) {
        // Create the isolated test schema (drop first if it exists)
        executeSQL(database, "USE DATABASE LTHDB")
        executeSQL(database, "USE WAREHOUSE LTHDB_TEST_WH")
        executeSQL(database, "DROP SCHEMA IF EXISTS ${schemaName} CASCADE")
        executeSQL(database, "CREATE SCHEMA ${schemaName}")
        executeSQL(database, "GRANT ALL PRIVILEGES ON SCHEMA ${schemaName} TO ROLE LIQUIBASE_TEST_HARNESS_ROLE")
    }
    
    private void cleanupSchema(DatabaseUnderTest database, String schemaName) {
        // First switch to the original schema, then drop the test schema
        executeSQL(database, "USE DATABASE LTHDB")
        executeSQL(database, "USE WAREHOUSE LTHDB_TEST_WH")
        executeSQL(database, "USE SCHEMA TESTHARNESS")
        executeSQL(database, "DROP SCHEMA IF EXISTS ${schemaName} CASCADE")
    }
    
    private void executeSQL(DatabaseUnderTest database, String sql) {
        Statement statement = null
        try {
            // Get the JDBC connection from the Liquibase database object
            def connection = database.database.getConnection()
            if (connection instanceof liquibase.database.jvm.JdbcConnection) {
                def conn = connection.getUnderlyingConnection()
                statement = conn.createStatement()
                statement.execute(sql)
            } else {
                throw new RuntimeException("Database connection is not a JDBC connection")
            }
        } finally {
            if (statement) {
                statement.close()
            }
        }
    }
}