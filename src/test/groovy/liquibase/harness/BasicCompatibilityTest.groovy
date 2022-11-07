package liquibase.harness

import liquibase.harness.config.TestConfig

class BasicCompatibilityTest extends liquibase.harness.compatibility.basic.BasicCompatibilityTest {

    static {
        //extensions don't distribute their files. Only we store them in src/main/resources.
        TestConfig.instance.outputResourcesBase = "src/main/resources"
    }
}
