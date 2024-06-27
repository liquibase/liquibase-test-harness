package liquibase.harness

import liquibase.harness.change.ChangeObjectTests
import liquibase.harness.data.ChangeDataTests
import org.junit.platform.suite.api.SelectClasses
import org.junit.platform.suite.api.Suite

@Suite
@SelectClasses([ChangeObjectTests, ChangeDataTests])
abstract class BaseHarnessSuite {
}
