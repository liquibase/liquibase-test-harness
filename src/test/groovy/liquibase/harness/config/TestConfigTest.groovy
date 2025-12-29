package liquibase.harness.config

import spock.lang.Specification

class TestConfigTest extends Specification {
    def setup() {
        // Reset singleton to allow test isolation
        TestConfig.@instance = null
    }

    def cleanup() {
        // Clear system properties set during test
        System.clearProperty("configFile")
        System.clearProperty("revalidateSql")
        System.clearProperty("prefix")
        // Reset singleton for other tests
        TestConfig.@instance = null
    }

    def "GetInstance"() {
        when: "system properties override default path to configFile and revalidateSql property"
        System.setProperty("configFile", "/self/test-config.yml")
        System.setProperty("revalidateSql", "false")
        System.setProperty("prefix", "docker")
        TestConfig overriddenTestConfig = TestConfig.getInstance()

        then: "values based on env variables should be used"
        overriddenTestConfig.context == "TestConfigTest"
        overriddenTestConfig.revalidateSql == false
        overriddenTestConfig.getFilteredDatabasesUnderTest().size() == 2
    }

}
