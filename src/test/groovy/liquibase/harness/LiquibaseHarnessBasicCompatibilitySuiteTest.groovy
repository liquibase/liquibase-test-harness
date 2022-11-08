package liquibase.harness

import liquibase.harness.config.TestConfig

class LiquibaseHarnessBasicCompatibilitySuiteTest extends BasicCompatibilitySuite {

    static {
        TestConfig.instance.outputResourcesBase = "src/main/resources"
    }
}
