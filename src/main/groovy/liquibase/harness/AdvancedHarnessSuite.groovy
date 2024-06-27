package liquibase.harness

import liquibase.harness.generateChangelog.GenerateChangelogTest
import liquibase.harness.snapshot.SnapshotObjectTests
import org.junit.platform.suite.api.SelectClasses
import org.junit.platform.suite.api.Suite

@Suite
@SelectClasses([GenerateChangelogTest, SnapshotObjectTests])
abstract class AdvancedHarnessSuite {
}
