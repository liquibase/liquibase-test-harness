package liquibase.sdk.test.snapshot

import groovy.transform.Canonical
import liquibase.snapshot.DatabaseSnapshot

import java.util.function.Function

class SnapshotTest {

    protected test(TestConfig... config) {

    }

    @Canonical
    static class TestConfig {
        String setup
        String cleanup

        Function<DatabaseSnapshot, Void> verify
    }
}
