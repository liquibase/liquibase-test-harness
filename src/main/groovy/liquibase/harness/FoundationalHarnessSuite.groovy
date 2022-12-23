package liquibase.harness


import liquibase.harness.compatibility.foundational.FoundationalTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite.class)
@Suite.SuiteClasses([FoundationalTest])
abstract class FoundationalHarnessSuite {
}
