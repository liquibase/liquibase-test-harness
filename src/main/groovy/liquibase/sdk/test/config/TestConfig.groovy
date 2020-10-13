package liquibase.sdk.test.config

import groovy.transform.ToString;
@ToString
class TestConfig {
    String inputFormat
    String context
    List<DatabaseUnderTest> databasesUnderTest
    List<String> defaultChangeObjects
}
