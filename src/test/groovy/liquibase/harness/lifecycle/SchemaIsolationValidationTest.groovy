package liquibase.harness.lifecycle

import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.util.TestUtils
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import spock.lang.Specification
import java.sql.DriverManager

/**
 * Simple validation test to prove schema isolation works.
 */
class SchemaIsolationValidationTest extends Specification {
    
    def "Validate schema isolation creates and uses isolated schema"() {
        given: "Direct database connection"
        def url = "jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LTHDB&warehouse=LTHDB_TEST_WH&schema=TESTHARNESS&role=LIQUIBASE_TEST_HARNESS_ROLE"
        def username = "COMMUNITYKEVIN"
        def password = "uQ1lAjwVisliu8CpUTVh0UnxoTUk3"
        
        // Create connection
        def conn = DriverManager.getConnection(url, username, password)
        def database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn))
        
        def databaseUnderTest = new DatabaseUnderTest(
            name: "snowflake",
            useSchemaIsolation: true,
            database: database
        )
        
        def hook = new SchemaIsolationHook()
        def context = new TestContext(databaseUnderTest, "ValidationTest", "testIsolation")
        
        when: "Hook creates isolated schema"
        println("\n=== SCHEMA ISOLATION VALIDATION TEST ===")
        println("Original schema: ${database.getDefaultSchemaName()}")
        
        hook.beforeTest(context)
        
        def isolatedSchema = context.getMetadata("testSchema")
        def currentSchema = database.getDefaultSchemaName()
        
        println("Isolated schema created: ${isolatedSchema}")
        println("Current default schema: ${currentSchema}")
        
        // Create a test table in the isolated schema
        def statement = conn.createStatement()
        statement.execute("CREATE TABLE TEST_VALIDATION (ID INT)")
        println("Created table TEST_VALIDATION in schema: ${currentSchema}")
        
        // Query to verify table exists in isolated schema
        def rs = statement.executeQuery("""
            SELECT TABLE_SCHEMA, TABLE_NAME 
            FROM INFORMATION_SCHEMA.TABLES 
            WHERE TABLE_NAME = 'TEST_VALIDATION'
        """)
        
        def tableSchema = null
        if (rs.next()) {
            tableSchema = rs.getString("TABLE_SCHEMA")
            println("Table found in schema: ${tableSchema}")
        }
        
        then: "Schema isolation should work"
        isolatedSchema == "TEST_TESTISOLATION"
        currentSchema == "TEST_TESTISOLATION"
        tableSchema == "TEST_TESTISOLATION"
        
        when: "Cleanup"
        hook.afterTest(context)
        println("Cleanup completed")
        
        then: "Cleanup succeeds"
        noExceptionThrown()
        
        cleanup:
        statement?.close()
        conn?.close()
        database?.close()
    }
}