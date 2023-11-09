package liquibase.harness

import liquibase.harness.compatibility.advanced.AdvancedTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite.class)
@Suite.SuiteClasses([AdvancedTest])
abstract class AdvancedHarnessSuite {
}
