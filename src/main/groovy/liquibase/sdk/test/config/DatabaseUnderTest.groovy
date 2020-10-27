package liquibase.sdk.test.config

import liquibase.database.Database;

class DatabaseUnderTest {
    Database database
    String version
    String name
    String username
    String password
    String url
    String dbSchema
}
