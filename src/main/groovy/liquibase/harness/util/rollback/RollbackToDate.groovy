package liquibase.harness.util.rollback;

import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.util.TestUtils

import java.text.SimpleDateFormat;

class RollbackToDate implements RollbackStrategy {
    String rollbackDateTime;

    @Override
    void prepareForRollback(List<DatabaseUnderTest> databases) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
        rollbackDateTime = sdf.format(new Date(System.currentTimeMillis() - 1000))
    }

    @Override
    void performRollback(Map<String, Object> commandArgs) {
        commandArgs.put("date", rollbackDateTime)
        TestUtils.executeCommandScope("rollbackToDate", commandArgs)
    }

    @Override
    void cleanupDatabase(List<DatabaseUnderTest> databases) {
        // No cleanup is needed for rollbackToDate
    }
}
