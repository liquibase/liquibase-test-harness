package liquibase.harness.util.rollback;

import liquibase.harness.config.DatabaseUnderTest;

interface RollbackStrategy {
    void prepareForRollback(List<DatabaseUnderTest> databases);

    void performRollback(Map<String, Object> commandArgs);

    void cleanupDatabase(List<DatabaseUnderTest> databases)
}
