package liquibase.harness.config

import groovy.transform.ToString
import liquibase.database.Database

@ToString
class DatabaseUnderTest {
    Database database
    String version
    String prefix
    String name
    String username
    String password
    String url
    String dbSchema
    String initScript
    boolean useSchemaIsolation = false
}
