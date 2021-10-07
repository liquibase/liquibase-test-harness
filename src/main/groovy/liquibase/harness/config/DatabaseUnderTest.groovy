package liquibase.harness.config

import liquibase.database.Database

class DatabaseUnderTest {
    Database database
    String version
    String prefix
    String name
    String username
    String password
    String url
    String dbSchema
}
