package liquibase.sdk.test.config

import groovy.transform.builder.Builder
import liquibase.database.Database;

@Builder
class TestInput {
     String databaseName
     String url
     String dbSchema
     String username
     String password
     String version
     String context
     String changeObject
     String pathToChangeLogFile

     Database database
}
