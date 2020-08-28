package liquibase.harness.config;

class TestConfig {
    String inputFormat
    String context
    List<DatabaseUnderTest> databasesUnderTest
    List<String> defaultChangeObjects
}
