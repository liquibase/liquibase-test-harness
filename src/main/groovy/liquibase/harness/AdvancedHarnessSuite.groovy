package liquibase.harness


import liquibase.harness.compatibility.generateChangelog.GenerateChangelogTest
import liquibase.harness.snapshot.SnapshotObjectTests
import liquibase.harness.diff.DiffCommandTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite.class)
@Suite.SuiteClasses([GenerateChangelogTest, SnapshotObjectTests, DiffCommandTest])
abstract class AdvancedHarnessSuite {
}
