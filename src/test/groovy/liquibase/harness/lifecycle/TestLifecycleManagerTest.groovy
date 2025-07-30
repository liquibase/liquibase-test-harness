package liquibase.harness.lifecycle

import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.harness.config.LifecycleHooksConfig
import spock.lang.Specification

class TestLifecycleManagerTest extends Specification {
    
    def setupSpec() {
        // Reset singleton for each test run
        TestLifecycleManager.instance = null
    }
    
    def cleanup() {
        // Clean up system properties after each test
        System.clearProperty("liquibase.harness.lifecycle.enabled")
        System.clearProperty("liquibase.harness.lifecycle.schemaIsolation")
        TestLifecycleManager.instance = null
    }
    
    def "TestLifecycleManager should be a singleton"() {
        when:
        def instance1 = TestLifecycleManager.getInstance()
        def instance2 = TestLifecycleManager.getInstance()
        
        then:
        instance1.is(instance2)
    }
    
    def "TestLifecycleManager should respect system property for enabling hooks"() {
        given:
        System.setProperty("liquibase.harness.lifecycle.enabled", "true")
        
        when:
        def manager = TestLifecycleManager.getInstance()
        manager.initialize()
        
        then:
        manager.isEnabled() == true
    }
    
    def "TestLifecycleManager should respect config file for enabling hooks"() {
        given:
        def config = Mock(TestConfig)
        def lifecycleConfig = new LifecycleHooksConfig()
        lifecycleConfig.enabled = true
        config.lifecycleHooks >> lifecycleConfig
        TestConfig.metaClass.static.getInstance = { -> config }
        
        when:
        def manager = TestLifecycleManager.getInstance()
        manager.initialize()
        
        then:
        manager.isEnabled() == true
        
        cleanup:
        TestConfig.metaClass = null
    }
    
    def "TestLifecycleManager should prioritize system property over config file"() {
        given:
        System.setProperty("liquibase.harness.lifecycle.enabled", "false")
        def config = Mock(TestConfig)
        def lifecycleConfig = new LifecycleHooksConfig()
        lifecycleConfig.enabled = true
        config.lifecycleHooks >> lifecycleConfig
        TestConfig.metaClass.static.getInstance = { -> config }
        
        when:
        def manager = TestLifecycleManager.getInstance()
        manager.initialize()
        
        then:
        manager.isEnabled() == false
        
        cleanup:
        TestConfig.metaClass = null
    }
    
    def "TestLifecycleManager should register SchemaIsolationHook when schema isolation is enabled"() {
        given:
        System.setProperty("liquibase.harness.lifecycle.enabled", "true")
        System.setProperty("liquibase.harness.lifecycle.schemaIsolation", "true")
        
        when:
        def manager = TestLifecycleManager.getInstance()
        manager.initialize()
        
        then:
        manager.hooks.size() > 0
        manager.hooks.any { it instanceof SchemaIsolationHook }
    }
    
    def "TestLifecycleManager should call beforeTest on supported hooks"() {
        given:
        def mockHook = Mock(TestLifecycleHook)
        def database = new DatabaseUnderTest(name: "snowflake", useSchemaIsolation: true)
        def context = new TestContext(database, "TestClass", "testMethod")
        
        mockHook.supports(database) >> true
        
        def manager = TestLifecycleManager.getInstance()
        manager.enabled = true
        manager.hooks = [mockHook]
        
        when:
        manager.beforeTest(context)
        
        then:
        1 * mockHook.beforeTest(context)
    }
    
    def "TestLifecycleManager should not call beforeTest on unsupported hooks"() {
        given:
        def mockHook = Mock(TestLifecycleHook)
        def database = new DatabaseUnderTest(name: "postgres", useSchemaIsolation: false)
        def context = new TestContext(database, "TestClass", "testMethod")
        
        mockHook.supports(database) >> false
        
        def manager = TestLifecycleManager.getInstance()
        manager.enabled = true
        manager.hooks = [mockHook]
        
        when:
        manager.beforeTest(context)
        
        then:
        0 * mockHook.beforeTest(context)
    }
    
    def "TestLifecycleManager should handle hook exceptions gracefully by default"() {
        given:
        def mockHook = Mock(TestLifecycleHook)
        def database = new DatabaseUnderTest(name: "snowflake", useSchemaIsolation: true)
        def context = new TestContext(database, "TestClass", "testMethod")
        
        mockHook.supports(database) >> true
        mockHook.beforeTest(context) >> { throw new RuntimeException("Hook failed") }
        
        def manager = TestLifecycleManager.getInstance()
        manager.enabled = true
        manager.hooks = [mockHook]
        
        when:
        manager.beforeTest(context)
        
        then:
        noExceptionThrown()
    }
    
    def "TestLifecycleManager should throw hook exceptions when failOnError is true"() {
        given:
        System.setProperty("liquibase.harness.lifecycle.failOnError", "true")
        def mockHook = Mock(TestLifecycleHook)
        def database = new DatabaseUnderTest(name: "snowflake", useSchemaIsolation: true)
        def context = new TestContext(database, "TestClass", "testMethod")
        
        mockHook.supports(database) >> true
        mockHook.beforeTest(context) >> { throw new RuntimeException("Hook failed") }
        
        def manager = TestLifecycleManager.getInstance()
        manager.enabled = true
        manager.hooks = [mockHook]
        
        when:
        manager.beforeTest(context)
        
        then:
        thrown(RuntimeException)
        
        cleanup:
        System.clearProperty("liquibase.harness.lifecycle.failOnError")
    }
}