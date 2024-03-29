package liquibase.harness

import liquibase.harness.config.TestConfig

class ChangeObjectTests extends liquibase.harness.change.ChangeObjectTests {

    static {
        //extensions don't distribute their files. Only we store them in src/main/resources.
        TestConfig.instance.outputResourcesBase = "src/main/resources"
    }
}
