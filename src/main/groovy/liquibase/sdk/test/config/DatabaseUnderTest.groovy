package liquibase.sdk.test.config;

class DatabaseUnderTest {
    String name
    String username
    String password
    List<DatabaseVersion> versions
    List<String> databaseSpecificChangeObjects
    String dbSchema
}
