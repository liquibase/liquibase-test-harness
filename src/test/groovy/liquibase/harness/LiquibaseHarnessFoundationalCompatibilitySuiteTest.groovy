package liquibase.harness

import liquibase.harness.config.TestConfig

class LiquibaseHarnessFoundationalCompatibilitySuiteTest extends FoundationalCompatibilitySuite {

    static {
        TestConfig.instance.outputResourcesBase = "src/main/resources"
    }
}
