package liquibase.harness

import liquibase.harness.compatibility.advanced.AdvancedTest
import liquibase.harness.generateChangelog.GenerateChangelogTest
import liquibase.harness.snapshot.SnapshotObjectTests
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite.class)
@Suite.SuiteClasses([GenerateChangelogTest, SnapshotObjectTests])
abstract class AdvancedHarnessSuite {
}
