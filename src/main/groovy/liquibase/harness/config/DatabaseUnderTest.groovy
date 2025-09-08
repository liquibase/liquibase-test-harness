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
    
    // Cloud database initialization properties
    String initScript
    String initChangelog
    Map<String, String> initProperties = [:]
    boolean skipInit = false
    
    // Schema isolation configuration
    boolean useSchemaIsolation = false
}
