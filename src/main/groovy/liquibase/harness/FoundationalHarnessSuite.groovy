package liquibase.harness

import liquibase.harness.compatibility.foundational.FoundationalTest
import org.junit.platform.suite.api.SelectClasses
import org.junit.platform.suite.api.Suite

@Suite
@SelectClasses([FoundationalTest])
abstract class FoundationalHarnessSuite {
}
