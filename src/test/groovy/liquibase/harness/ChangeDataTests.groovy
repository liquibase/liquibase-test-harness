package liquibase.harness

import liquibase.harness.config.TestConfig

class ChangeDataTests extends liquibase.harness.data.ChangeDataTests {

    static {
        //extensions don't distribute their files. Only we store them in src/main/resources.
        TestConfig.instance.outputResourcesBase = "src/main/resources"
    }
}
