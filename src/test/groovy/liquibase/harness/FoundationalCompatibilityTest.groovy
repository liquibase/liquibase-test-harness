package liquibase.harness

import liquibase.harness.config.TestConfig

class FoundationalCompatibilityTest extends liquibase.harness.compatibility.foundational.FoundationalCompatibilityTest {

    static {
        //extensions don't distribute their files. Only we store them in src/main/resources.
        TestConfig.instance.outputResourcesBase = "src/main/resources"
    }
}
