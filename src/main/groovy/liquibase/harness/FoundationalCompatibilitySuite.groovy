package liquibase.harness

import liquibase.harness.compatibility.basic.BasicCompatibilityTest
import liquibase.harness.compatibility.foundational.FoundationalCompatibilityTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite.class)
@Suite.SuiteClasses([BasicCompatibilityTest, FoundationalCompatibilityTest])
abstract class FoundationalCompatibilitySuite {
}
