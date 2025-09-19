package liquibase.harness.lifecycle

import liquibase.harness.config.DatabaseUnderTest

/**
 * Interface for test lifecycle hooks that run before/after each test.
 * Implementations can provide database-specific setup and cleanup logic.
 */
interface TestLifecycleHook {
    /**
     * Executed before each test method
     * @param testContext Contains test metadata and database connection info
     */
    void beforeTest(TestContext testContext)
    
    /**
     * Executed after each test method
     * @param testContext Contains test metadata and database connection info
     */
    void afterTest(TestContext testContext)
    
    /**
     * Determines if this hook should run for the given database
     * @param database The database under test
     * @return true if this hook supports the database
     */
    boolean supports(DatabaseUnderTest database)
}