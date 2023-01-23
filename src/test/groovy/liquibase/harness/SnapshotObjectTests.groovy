package liquibase.harness

import liquibase.harness.config.TestConfig

class SnapshotObjectTests extends liquibase.harness.snapshot.SnapshotObjectTests {

    static {
        //extensions don't distribute their files. Only we store them in src/main/resources.
        TestConfig.instance.outputResourcesBase = "src/main/resources"
    }
}
