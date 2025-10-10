package liquibase.harness

import liquibase.harness.change.ChangeObjectTests
import liquibase.harness.data.ChangeDataTests
import liquibase.harness.diff.DiffChangelogTests
import liquibase.harness.diff.DiffTests
import liquibase.harness.generateChangelog.GenerateChangelogTest
import liquibase.harness.snapshot.SnapshotObjectTests
import org.junit.platform.suite.api.SelectClasses
import org.junit.platform.suite.api.Suite

@Suite
@SelectClasses([
    ChangeObjectTests,
    ChangeDataTests,
    GenerateChangelogTest,
    SnapshotObjectTests,
    DiffTests,
    DiffChangelogTests
])
abstract class AdvancedHarnessSuite {
}
