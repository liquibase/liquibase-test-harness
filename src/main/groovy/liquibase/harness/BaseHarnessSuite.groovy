package liquibase.harness

import liquibase.harness.change.ChangeObjectTests
import liquibase.harness.data.ChangeDataTests
import liquibase.harness.snapshot.SnapshotObjectTests
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite.class)
@Suite.SuiteClasses([ChangeObjectTests, ChangeDataTests, SnapshotObjectTests])
abstract class BaseHarnessSuite {
}
