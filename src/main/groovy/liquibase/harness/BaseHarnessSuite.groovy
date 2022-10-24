package liquibase.harness

import liquibase.harness.change.ChangeObjectTests
import liquibase.harness.data.ChangeDataTests
import liquibase.harness.snapshot.SnapshotObjectTests
import liquibase.harness.compatibility.basic.BasicCompatibilityTest
import liquibase.harness.compatibility.foundational.FoundationalCompatibilityTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite.class)
@Suite.SuiteClasses([BasicCompatibilityTest, FoundationalCompatibilityTest])
abstract class BaseHarnessSuite {
}
