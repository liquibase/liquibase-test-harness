package liquibase.harness.init

import liquibase.harness.config.DatabaseUnderTest
import liquibase.database.Database
import liquibase.database.jvm.JdbcConnection
import spock.lang.Specification
import java.sql.Connection
import java.sql.Statement
import java.sql.SQLException
import net.bytebuddy.ByteBuddy
import net.bytebuddy.implementation.MethodDelegation

class CloudDatabaseInitializerTest extends Specification {
    
    def setup() {
        // Reset the singleton state before each test
        CloudDatabaseInitializer.instance.reset()
    }
    
    def "CloudDatabaseInitializer should be a singleton"() {
        when:
        def instance1 = CloudDatabaseInitializer.getInstance()
        def instance2 = CloudDatabaseInitializer.getInstance()
        
        then:
        instance1.is(instance2)
    }
    
    def "CloudDatabaseInitializer should detect cloud databases correctly"() {
        expect:
        CloudDatabaseInitializer.isCloudDatabase(new DatabaseUnderTest(url: url)) == expected
        
        where:
        url                                                          | expected
        "jdbc:snowflake://test.snowflakecomputing.com/db"          | true
        "jdbc:mysql://mydb.rds.amazonaws.com:3306/test"            | true
        "jdbc:postgresql://mydb.cluster-123.amazonaws.com:5432/db"  | true
        "jdbc:sqlserver://mydb.database.windows.net:1433"          | true
        "jdbc:mysql://mydb.database.azure.com:3306/test"           | true
        "jdbc:oracle://mydb.oraclecloud.com:1521/pdb"             | true
        "jdbc:postgresql://mydb.db.aiven.io:12345/defaultdb"       | true
        "jdbc:mysql://localhost:3306/test"                         | false
        "jdbc:h2:mem:test"                                         | false
        "jdbc:postgresql://127.0.0.1:5432/test"                    | false
        null                                                        | false
    }
    
    def "CloudDatabaseInitializer should skip initialization for non-cloud databases"() {
        given:
        def database = new DatabaseUnderTest(
            name: "h2",
            url: "jdbc:h2:mem:test"
        )
        def initializer = CloudDatabaseInitializer.getInstance()
        
        when:
        def result = initializer.initializeIfNeeded(database)
        
        then:
        result == false
    }
    
    def "CloudDatabaseInitializer should skip when no init configuration"() {
        given:
        def database = new DatabaseUnderTest(
            name: "snowflake",
            url: "jdbc:snowflake://test.snowflakecomputing.com/db"
            // No initScript or initChangelog
        )
        def initializer = CloudDatabaseInitializer.getInstance()
        
        when:
        def result = initializer.initializeIfNeeded(database)
        
        then:
        result == false
    }
    
    def "CloudDatabaseInitializer should skip when skipInit is true"() {
        given:
        def database = new DatabaseUnderTest(
            name: "snowflake",
            url: "jdbc:snowflake://test.snowflakecomputing.com/db",
            initScript: "init.sql",
            skipInit: true
        )
        def initializer = CloudDatabaseInitializer.getInstance()
        
        when:
        def result = initializer.initializeIfNeeded(database)
        
        then:
        result == false
    }
    
    def "CloudDatabaseInitializer should initialize only once per database"() {
        given:
        def database = new DatabaseUnderTest(
            name: "snowflake",
            url: "jdbc:snowflake://test.snowflakecomputing.com/db",
            initScript: "test-init.sql"
        )
        
        def initializer = CloudDatabaseInitializer.getInstance()
        def executionCount = 0
        
        // Create a test version that tracks executions
        def testInitializer = new CloudDatabaseInitializer() {
            @Override
            protected void executeInitScript(DatabaseUnderTest db) {
                executionCount++
                // Mock successful execution
            }
        }
        
        when: "First initialization"
        def result1 = testInitializer.initializeIfNeeded(database)
        
        then:
        result1 == true
        executionCount == 1
        
        when: "Second initialization attempt"
        def result2 = testInitializer.initializeIfNeeded(database)
        
        then:
        result2 == false
        executionCount == 1  // Should not execute again
    }
    
    def "CloudDatabaseInitializer should handle SQL script execution"() {
        given:
        def mockConnection = Mock(Connection)
        def mockStatement = Mock(Statement)
        mockConnection.createStatement() >> mockStatement
        
        def database = new DatabaseUnderTest(
            name: "snowflake",
            url: "jdbc:snowflake://test.snowflakecomputing.com/db"
        )
        
        def testInitializer = new CloudDatabaseInitializer() {
            @Override
            protected Connection createConnection(DatabaseUnderTest db) {
                return mockConnection
            }
            
            @Override
            protected void logDebug(String message) {
                println "DEBUG: ${message}"  // Print debug messages to see what's happening
            }
        }
        
        def sqlContent = """
        -- Comment line
        CREATE SCHEMA TEST_SCHEMA;
        
        GRANT ALL ON SCHEMA TEST_SCHEMA TO ROLE TEST_ROLE;
        
        -- Another comment
        USE SCHEMA TEST_SCHEMA;
        """
        
        when:
        testInitializer.executeSQLScript(database, sqlContent)
        
        then:
        // More flexible matching since SQL parsing might add/remove whitespace
        1 * mockStatement.execute({ it.contains("CREATE SCHEMA TEST_SCHEMA") })
        1 * mockStatement.execute({ it.contains("GRANT ALL ON SCHEMA TEST_SCHEMA TO ROLE TEST_ROLE") })
        1 * mockStatement.execute({ it.contains("USE SCHEMA TEST_SCHEMA") })
        1 * mockStatement.close()
        1 * mockConnection.close()
    }
    
    def "CloudDatabaseInitializer should continue on SQL errors when configured"() {
        given:
        System.setProperty("liquibase.harness.cloud.init.continueOnSqlError", "true")
        
        def mockConnection = Mock(Connection)
        def mockStatement = Mock(Statement)
        mockConnection.createStatement() >> mockStatement
        
        def database = new DatabaseUnderTest(
            name: "snowflake",
            url: "jdbc:snowflake://test.snowflakecomputing.com/db"
        )
        
        def testInitializer = new CloudDatabaseInitializer() {
            @Override
            protected Connection createConnection(DatabaseUnderTest db) {
                return mockConnection
            }
        }
        
        def sqlContent = """
        CREATE SCHEMA TEST;
        USE SCHEMA TEST;
        """
        
        when:
        testInitializer.executeSQLScript(database, sqlContent)
        
        then:
        noExceptionThrown()
        1 * mockStatement.execute("CREATE SCHEMA TEST") >> { throw new SQLException("Schema exists") }
        1 * mockStatement.execute("USE SCHEMA TEST")
        1 * mockStatement.close()
        1 * mockConnection.close()
        
        cleanup:
        System.clearProperty("liquibase.harness.cloud.init.continueOnSqlError")
    }
    
    def "CloudDatabaseInitializer should fail on SQL errors when configured"() {
        given:
        System.setProperty("liquibase.harness.cloud.init.continueOnSqlError", "false")
        
        def mockConnection = Mock(Connection)
        def mockStatement = Mock(Statement)
        mockConnection.createStatement() >> mockStatement
        mockStatement.execute(_) >> { throw new SQLException("Syntax error") }
        
        def database = new DatabaseUnderTest(
            name: "snowflake",
            url: "jdbc:snowflake://test.snowflakecomputing.com/db"
        )
        
        def initializer = CloudDatabaseInitializer.getInstance()
        
        when:
        initializer.executeSQLScript(database, "INVALID SQL")
        
        then:
        thrown(SQLException)
        
        cleanup:
        System.clearProperty("liquibase.harness.cloud.init.continueOnSqlError")
    }
    
    def "CloudDatabaseInitializer should generate unique database keys"() {
        given:
        def initializer = CloudDatabaseInitializer.getInstance()
        
        def db1 = new DatabaseUnderTest(
            name: "snowflake",
            url: "jdbc:snowflake://test1.snowflakecomputing.com/db",
            version: "1.0"
        )
        
        def db2 = new DatabaseUnderTest(
            name: "snowflake",
            url: "jdbc:snowflake://test2.snowflakecomputing.com/db",
            version: "1.0"
        )
        
        def db3 = new DatabaseUnderTest(
            name: "snowflake",
            url: "jdbc:snowflake://test1.snowflakecomputing.com/db",
            version: "2.0"
        )
        
        when:
        def key1 = initializer.generateDatabaseKey(db1)
        def key2 = initializer.generateDatabaseKey(db2)
        def key3 = initializer.generateDatabaseKey(db3)
        
        then:
        key1 != key2  // Different URLs
        key1 != key3  // Different versions
        key2 != key3  // Different URLs and versions
    }
}