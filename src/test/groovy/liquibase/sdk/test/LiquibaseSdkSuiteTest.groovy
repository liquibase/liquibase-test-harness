package liquibase.sdk.test

import liquibase.sdk.test.config.TestConfig

class LiquibaseSdkSuiteTest extends BaseLiquibaseSdkSuite {

    static {
        //extensions don't distribute their files. Only we store them in src/main/resources.
        TestConfig.instance.outputResourcesBase = "src/main/resources"
    }
}
