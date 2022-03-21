package liquibase.harness.util.rollback

import liquibase.Scope
import liquibase.database.jvm.JdbcConnection;
import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.util.TestUtils

class RollbackByTag implements RollbackStrategy{
    String tag = "test-harness-tag"
    @Override
    void prepareForRollback(List<DatabaseUnderTest> databases) {
        for (DatabaseUnderTest database : databases) {
            Map<String, Object> argsMap = new HashMap()
            argsMap.put("url", database.url)
            argsMap.put("username", database.username)
            argsMap.put("password", database.password)
            argsMap.put("tag", tag)
            TestUtils.executeCommandScope("tag", argsMap)
        }
    }

    @Override
    void performRollback(Map<String, Object> commandArgs) {
        commandArgs.put("tag", tag)
        TestUtils.executeCommandScope("rollback", commandArgs)

    }

    @Override
    void cleanupDatabase(List<DatabaseUnderTest> databases) {
        for (DatabaseUnderTest databaseUnderTest : databases) {
            def connection = databaseUnderTest.database.getConnection()
            try {
                ((JdbcConnection) connection).createStatement().executeUpdate("delete from DATABASECHANGELOG where TAG='${tag}'")
            } catch (Exception Exception) {
                Scope.getCurrentScope().getUI().sendMessage("Couldn't delete ${tag} tag from tracking table " +
                        Exception.printStackTrace())
            } finally {
                connection.commit()
            }
        }
    }
}
