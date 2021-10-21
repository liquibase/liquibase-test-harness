package liquibase.harness.config

import spock.lang.Specification

class TestConfigTest extends Specification {
    def "GetInstance"() {
        when: "system properties override default path to configFile and revalidateSql property"
        System.setProperty("configFile", "/self/test-config.yml")
        System.setProperty("revalidateSql", "false")
        TestConfig overriddenTestConfig = TestConfig.getInstance()

        then: "this values should be used"
        overriddenTestConfig.context == "TestConfigTest"
        overriddenTestConfig.revalidateSql == false
    }

}
