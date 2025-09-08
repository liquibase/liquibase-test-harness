package liquibase.harness.lifecycle

import liquibase.harness.config.DatabaseUnderTest

/**
 * Context information passed to lifecycle hooks.
 * Contains all the information a hook might need about the current test.
 */
class TestContext {
    DatabaseUnderTest database
    String testMethodName
    String testClassName
    Map<String, Object> metadata = [:]
    
    /**
     * Constructor
     * @param database The database being tested
     * @param testClassName The name of the test class
     * @param testMethodName The name of the test method
     */
    TestContext(DatabaseUnderTest database, String testClassName, String testMethodName) {
        this.database = database
        this.testClassName = testClassName
        this.testMethodName = testMethodName
    }
    
    /**
     * Add metadata to the context
     * @param key The metadata key
     * @param value The metadata value
     */
    void addMetadata(String key, Object value) {
        metadata[key] = value
    }
    
    /**
     * Get metadata from the context
     * @param key The metadata key
     * @return The metadata value, or null if not found
     */
    Object getMetadata(String key) {
        return metadata[key]
    }
}