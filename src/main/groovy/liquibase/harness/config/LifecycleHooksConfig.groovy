package liquibase.harness.config

import groovy.transform.ToString

@ToString
class LifecycleHooksConfig {
    Boolean enabled = false
    Boolean schemaIsolation = false
    Boolean failOnError = false
}