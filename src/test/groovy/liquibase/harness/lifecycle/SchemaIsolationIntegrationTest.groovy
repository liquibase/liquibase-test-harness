package liquibase.harness.lifecycle

import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.config.LifecycleHooksConfig
import liquibase.harness.util.TestUtils
import spock.lang.Specification
import spock.lang.IgnoreIf

/**
 * Integration test for schema isolation functionality.
 * This test requires a running Snowflake instance and proper credentials.
 */
@IgnoreIf({ !System.getenv("SNOWFLAKE_TEST_URL") })
class SchemaIsolationIntegrationTest extends Specification {
    
    def setupSpec() {
        // Ensure lifecycle hooks are enabled for integration tests
        System.setProperty("liquibase.harness.lifecycle.enabled", "true")
    }
    
    def cleanupSpec() {
        System.clearProperty("liquibase.harness.lifecycle.enabled")
    }
    
    def "Schema isolation should create isolated schema for test execution"() {
        given: "A Snowflake database with schema isolation enabled"
        def database = new DatabaseUnderTest(
            name: "snowflake",
            url: System.getenv("SNOWFLAKE_TEST_URL") ?: "jdbc:snowflake://test.snowflakecomputing.com",
            username: System.getenv("SNOWFLAKE_TEST_USER") ?: "testuser",
            password: System.getenv("SNOWFLAKE_TEST_PASSWORD") ?: "testpass",
            useSchemaIsolation: true
        )
        
        // Initialize the database connection
        TestUtils.connectToDatabaseWithRetry(database, 1)
        
        def context = new TestContext(database, "IntegrationTest", "testSchemaIsolation")
        def manager = TestLifecycleManager.getInstance()
        manager.initialize()
        
        when: "Lifecycle hooks are executed"
        manager.beforeTest(context)
        
        then: "An isolated schema should be created"
        def schemaName = context.getMetadata("testSchema")
        schemaName != null
        schemaName == "TEST_TESTSCHEMAISOLATION"
        
        and: "The schema should exist in the database"
        schemaExists(database, schemaName)
        
        when: "Test completes and cleanup runs"
        manager.afterTest(context)
        
        then: "The isolated schema should be dropped"
        !schemaExists(database, schemaName)
        
        cleanup:
        database.database?.close()
    }
    
    def "Multiple tests should get different isolated schemas"() {
        given: "A Snowflake database with schema isolation"
        def database = new DatabaseUnderTest(
            name: "snowflake",
            url: System.getenv("SNOWFLAKE_TEST_URL") ?: "jdbc:snowflake://test.snowflakecomputing.com",
            username: System.getenv("SNOWFLAKE_TEST_USER") ?: "testuser",
            password: System.getenv("SNOWFLAKE_TEST_PASSWORD") ?: "testpass",
            useSchemaIsolation: true
        )
        
        TestUtils.connectToDatabaseWithRetry(database, 1)
        
        def context1 = new TestContext(database, "IntegrationTest", "test1")
        def context2 = new TestContext(database, "IntegrationTest", "test2")
        
        def manager = TestLifecycleManager.getInstance()
        manager.initialize()
        
        when: "Two tests are run"
        manager.beforeTest(context1)
        def schema1 = context1.getMetadata("testSchema")
        
        manager.beforeTest(context2)
        def schema2 = context2.getMetadata("testSchema")
        
        then: "Each test gets a different schema"
        schema1 == "TEST_TEST1"
        schema2 == "TEST_TEST2"
        schema1 != schema2
        
        and: "Both schemas exist"
        schemaExists(database, schema1)
        schemaExists(database, schema2)
        
        cleanup:
        manager.afterTest(context1)
        manager.afterTest(context2)
        database.database?.close()
    }
    
    def "Configuration-based schema isolation should work"() {
        given: "A test configuration with lifecycle hooks enabled"
        def lifecycleConfig = new LifecycleHooksConfig()
        lifecycleConfig.enabled = true
        
        def config = Mock(TestConfig)
        config.lifecycleHooks >> lifecycleConfig
        TestConfig.metaClass.static.getInstance = { -> config }
        
        and: "A database with schema isolation"
        def database = new DatabaseUnderTest(
            name: "snowflake",
            url: System.getenv("SNOWFLAKE_TEST_URL") ?: "jdbc:snowflake://test.snowflakecomputing.com",
            username: System.getenv("SNOWFLAKE_TEST_USER") ?: "testuser",
            password: System.getenv("SNOWFLAKE_TEST_PASSWORD") ?: "testpass",
            useSchemaIsolation: true
        )
        
        TestUtils.connectToDatabaseWithRetry(database, 1)
        
        def context = new TestContext(database, "IntegrationTest", "configTest")
        
        // Clear system property to ensure config is used
        System.clearProperty("liquibase.harness.lifecycle.enabled")
        
        // Reset and reinitialize manager
        TestLifecycleManager.instance = null
        def manager = TestLifecycleManager.getInstance()
        manager.initialize()
        
        when: "Lifecycle hooks are executed"
        manager.beforeTest(context)
        
        then: "Schema isolation should work based on config"
        def schemaName = context.getMetadata("testSchema")
        schemaName == "TEST_CONFIGTEST"
        schemaExists(database, schemaName)
        
        cleanup:
        manager.afterTest(context)
        database.database?.close()
        TestConfig.metaClass = null
        System.setProperty("liquibase.harness.lifecycle.enabled", "true")
    }
    
    /**
     * Helper method to check if a schema exists
     */
    private boolean schemaExists(DatabaseUnderTest database, String schemaName) {
        def connection = database.database.connection.underlyingConnection
        def statement = connection.createStatement()
        try {
            def rs = statement.executeQuery(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '${schemaName}'"
            )
            rs.next()
            return rs.getInt(1) > 0
        } finally {
            statement.close()
        }
    }
}