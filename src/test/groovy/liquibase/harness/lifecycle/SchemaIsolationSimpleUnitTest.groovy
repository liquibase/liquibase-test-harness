package liquibase.harness.lifecycle

import liquibase.harness.config.DatabaseUnderTest
import spock.lang.Specification

/**
 * Simple unit tests for SchemaIsolationHook that don't require mocking.
 * Tests basic functionality like name sanitization and configuration checking.
 */
class SchemaIsolationSimpleUnitTest extends Specification {
    
    def "sanitizeTestName should handle various input correctly"() {
        given:
        def hook = new SchemaIsolationHook()
        
        expect:
        hook.sanitizeTestName(input) == expected
        
        where:
        input                    | expected
        "simple"                | "SIMPLE"
        "with-hyphens"         | "WITH_HYPHENS"
        "with.dots"            | "WITH_DOTS"
        "with spaces"          | "WITH_SPACES"
        "special@#\$%chars"    | "SPECIAL____CHARS"
        "MixedCase"            | "MIXEDCASE"
        ""                     | "UNKNOWN"
        null                   | "UNKNOWN"
        "123start"             | "_123START"
        "test__multiple_"      | "TEST__MULTIPLE_"
    }
    
    def "supports should correctly identify Snowflake databases with schema isolation"() {
        given:
        def hook = new SchemaIsolationHook()
        
        when:
        def database = new DatabaseUnderTest(
            name: dbName,
            useSchemaIsolation: useIsolation
        )
        
        then:
        hook.supports(database) == expected
        
        where:
        dbName       | useIsolation | expected
        "snowflake"  | true         | true
        "snowflake"  | false        | false
        "postgres"   | true         | false
        "mysql"      | true         | false
        null         | true         | false
        "snowflake"  | null         | false
    }
    
    def "ThreadLocal values should be properly managed"() {
        given:
        def hook = new SchemaIsolationHook()
        
        when: "Values are set"
        hook.currentTestSchema.set("TEST_SCHEMA")
        hook.originalSchema.set("ORIGINAL")
        
        then: "Values can be retrieved"
        hook.currentTestSchema.get() == "TEST_SCHEMA"
        hook.originalSchema.get() == "ORIGINAL"
        
        when: "Values are cleared"
        hook.currentTestSchema.set(null)
        hook.originalSchema.set(null)
        
        then: "Values are null"
        hook.currentTestSchema.get() == null
        hook.originalSchema.get() == null
    }
    
    def "isSnowflake should correctly identify Snowflake databases"() {
        given:
        def hook = new SchemaIsolationHook()
        
        expect:
        hook.isSnowflake(new DatabaseUnderTest(name: name)) == expected
        
        where:
        name                 | expected
        "snowflake"         | true
        "SNOWFLAKE"         | true
        "Snowflake"         | true
        "snowflake-test"    | true
        "postgres"          | false
        "mysql"             | false
        ""                  | false
        null                | false
    }
    
    def "schema name generation should be predictable"() {
        given:
        def hook = new SchemaIsolationHook()
        def testName = "myTestMethod"
        
        when:
        def schemaName = "TEST_" + hook.sanitizeTestName(testName)
        
        then:
        schemaName == "TEST_MYTESTMETHOD"
        schemaName.length() <= 255
        schemaName.matches("^[A-Z_][A-Z0-9_]*\$")
    }
    
    def "configuration properties should be accessible"() {
        given:
        def hook = new SchemaIsolationHook()
        
        expect:
        hook.maxRetries == 3
        hook.retryDelayMs == 1000
        
        when:
        hook.maxRetries = 5
        hook.retryDelayMs = 2000
        
        then:
        hook.maxRetries == 5
        hook.retryDelayMs == 2000
    }
}