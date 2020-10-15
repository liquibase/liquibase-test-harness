package liquibase.sdk.test

class LiquibaseSdkSuite extends BaseLiquibaseSdkSuite {

    static {
        //extensions don't distribute their files. Only we store them in src/main/resources.
        ChangeObjectTests.outputResourcesBase = "src/main/resources"
    }
}
