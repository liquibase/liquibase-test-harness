package liquibase.harness.config;

class TestConfig {
    String context
    List<DatabaseUnderTest> databasesUnderTest
    List<String> defaultChangeObjects
}
