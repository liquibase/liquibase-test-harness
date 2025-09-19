package liquibase.harness.lifecycle

import liquibase.harness.config.DatabaseUnderTest
import liquibase.harness.config.TestConfig
import liquibase.Scope

/**
 * Manages test lifecycle hooks.
 * This is the main entry point for lifecycle hook functionality.
 */
class TestLifecycleManager {
    
    private List<TestLifecycleHook> hooks = []
    private boolean enabled = false
    private static TestLifecycleManager instance
    
    /**
     * Get singleton instance
     */
    static TestLifecycleManager getInstance() {
        if (instance == null) {
            instance = new TestLifecycleManager()
        }
        return instance
    }
    
    private TestLifecycleManager() {
        initialize()
    }
    
    void initialize() {
        // Check if lifecycle hooks are enabled
        enabled = isLifecycleHooksEnabled()
        
        if (enabled) {
            logInfo("Test lifecycle hooks are enabled")
            
            // Load hooks via ServiceLoader for extensibility
            try {
                ServiceLoader.load(TestLifecycleHook.class).each { hook ->
                    hooks.add(hook)
                    logInfo("Registered lifecycle hook via ServiceLoader: ${hook.class.simpleName}")
                }
            } catch (Exception e) {
                logDebug("No custom lifecycle hooks found via ServiceLoader: ${e.message}")
            }
            
            // Add default hooks - both can coexist
            hooks.add(new SchemaIsolationHook())
            logInfo("Registered lifecycle hook: SchemaIsolationHook (schema isolation with parallel support)")
            
            hooks.add(new ScriptBasedLifecycleHook())
            logInfo("Registered default lifecycle hook: ScriptBasedLifecycleHook")
        } else {
            logDebug("Test lifecycle hooks are disabled")
        }
    }
    
    void beforeTest(TestContext context) {
        logInfo("TestLifecycleManager.beforeTest called - enabled: ${enabled}, context: ${context != null}, database: ${context?.database?.name}")
        if (!enabled || context == null || context.database == null) return
        
        logInfo("Found ${hooks.size()} lifecycle hooks")
        hooks.each { hook ->
            logInfo("Checking hook ${hook.class.simpleName} support for ${context.database.name}")
            if (hook.supports(context.database)) {
                try {
                    logInfo("Running pre-test hook ${hook.class.simpleName} for ${context.database.name}")
                    hook.beforeTest(context)
                } catch (Exception e) {
                    // Log but don't fail - maintain backward compatibility
                    logWarn("Pre-test hook ${hook.class.simpleName} failed: ${e.message}", e)
                    if (shouldFailOnHookError()) {
                        throw e
                    }
                }
            } else {
                logInfo("Hook ${hook.class.simpleName} does not support ${context.database.name}")
            }
        }
    }
    
    void afterTest(TestContext context) {
        if (!enabled || context == null || context.database == null) return
        
        hooks.each { hook ->
            if (hook.supports(context.database)) {
                try {
                    logDebug("Running post-test hook ${hook.class.simpleName} for ${context.database.name}")
                    hook.afterTest(context)
                } catch (Exception e) {
                    // Log but don't fail - maintain backward compatibility
                    logWarn("Post-test hook ${hook.class.simpleName} failed: ${e.message}", e)
                    if (shouldFailOnHookError()) {
                        throw e
                    }
                }
            }
        }
    }
    
    /**
     * Check if lifecycle hooks are enabled
     */
    private boolean isLifecycleHooksEnabled() {
        // Check system property first
        String sysProp = System.getProperty("liquibase.harness.lifecycle.enabled")
        if (sysProp != null) {
            return sysProp.equalsIgnoreCase("true")
        }
        
        // Check harness config
        try {
            def config = TestConfig.instance
            if (config?.lifecycleHooks?.enabled != null) {
                return config.lifecycleHooks.enabled
            }
        } catch (Exception e) {
            logDebug("Could not read lifecycle config from TestConfig: ${e.message}")
        }
        
        // Default to disabled for backward compatibility
        return false
    }
    
    private boolean shouldFailOnHookError() {
        String sysProp = System.getProperty("liquibase.harness.lifecycle.failOnError", "false")
        return sysProp.equalsIgnoreCase("true")
    }
    
    private boolean useSchemaIsolation() {
        String sysProp = System.getProperty("liquibase.harness.lifecycle.schemaIsolation", "false")
        return sysProp.equalsIgnoreCase("true")
    }
    
    /**
     * Check if schema isolation should be used for a specific test
     */
    private boolean useSchemaIsolationForTest(String testName) {
        // Always use schema isolation for tests with "_isolated" suffix
        if (testName?.endsWith("_isolated")) {
            return true
        }
        // Otherwise check system property
        return useSchemaIsolation()
    }
    
    /**
     * Reset the manager (useful for testing)
     */
    void reset() {
        hooks.clear()
        initialize()
    }
    
    /**
     * Check if hooks are enabled
     */
    boolean isEnabled() {
        return enabled
    }
    
    /**
     * Get registered hooks (for testing/debugging)
     */
    List<TestLifecycleHook> getHooks() {
        return new ArrayList<TestLifecycleHook>(hooks)
    }
    
    private void logInfo(String message) {
        try {
            Scope.getCurrentScope().getUI().sendMessage("[Lifecycle Manager] " + message)
        } catch (Exception e) {
            println("[Lifecycle Manager] " + message)
        }
    }
    
    private void logWarn(String message, Exception e = null) {
        try {
            Scope.getCurrentScope().getUI().sendMessage("[Lifecycle Manager] WARNING: " + message)
            if (e != null && System.getProperty("liquibase.harness.lifecycle.debug", "false") == "true") {
                Scope.getCurrentScope().getUI().sendMessage(e.toString())
            }
        } catch (Exception ex) {
            System.err.println("[Lifecycle Manager] WARNING: " + message)
            if (e != null && System.getProperty("liquibase.harness.lifecycle.debug", "false") == "true") {
                e.printStackTrace()
            }
        }
    }
    
    private void logDebug(String message) {
        if (System.getProperty("liquibase.harness.lifecycle.debug", "false") == "true") {
            logInfo("DEBUG: " + message)
        }
    }
}