package liquibase.harness.lifecycle

import liquibase.harness.config.TestConfig
import liquibase.harness.config.LifecycleHooksConfig
import org.yaml.snakeyaml.Yaml
import spock.lang.Specification

class LifecycleHooksConfigTest extends Specification {
    
    def "LifecycleHooksConfig should have correct default values"() {
        when:
        def config = new LifecycleHooksConfig()
        
        then:
        config.enabled == false
        config.schemaIsolation == false
        config.failOnError == false
    }
    
    def "TestConfig should properly parse lifecycleHooks configuration from YAML"() {
        given:
        def yamlContent = """
        inputFormat: xml
        context: testContext
        lifecycleHooks:
          enabled: true
          schemaIsolation: true
          failOnError: false
        databasesUnderTest:
          - name: snowflake
            version: latest
            url: jdbc:snowflake://test.snowflakecomputing.com
            username: testuser
            password: testpass
            useSchemaIsolation: true
        """
        
        when:
        def yaml = new Yaml()
        def config = yaml.loadAs(yamlContent, TestConfig.class)
        
        then:
        config.lifecycleHooks != null
        config.lifecycleHooks.enabled == true
        config.lifecycleHooks.schemaIsolation == true
        config.lifecycleHooks.failOnError == false
    }
    
    def "TestConfig should handle missing lifecycleHooks configuration"() {
        given:
        def yamlContent = """
        inputFormat: xml
        context: testContext
        databasesUnderTest:
          - name: h2
            version: latest
            url: jdbc:h2:mem:test
        """
        
        when:
        def yaml = new Yaml()
        def config = yaml.loadAs(yamlContent, TestConfig.class)
        
        then:
        config.lifecycleHooks == null
    }
    
    def "DatabaseUnderTest should properly parse useSchemaIsolation flag"() {
        given:
        def yamlContent = """
        inputFormat: xml
        databasesUnderTest:
          - name: snowflake
            version: latest
            url: jdbc:snowflake://test.snowflakecomputing.com
            useSchemaIsolation: true
          - name: postgres
            version: latest
            url: jdbc:postgresql://localhost:5432/test
            useSchemaIsolation: false
          - name: mysql
            version: latest
            url: jdbc:mysql://localhost:3306/test
            # useSchemaIsolation not specified, should default to false
        """
        
        when:
        def yaml = new Yaml()
        def config = yaml.loadAs(yamlContent, TestConfig.class)
        
        then:
        config.databasesUnderTest.size() == 3
        config.databasesUnderTest[0].useSchemaIsolation == true
        config.databasesUnderTest[1].useSchemaIsolation == false
        config.databasesUnderTest[2].useSchemaIsolation == false  // default value
    }
    
    def "Full configuration should work with all lifecycle features"() {
        given:
        def yamlContent = """
        inputFormat: xml
        context: testContext
        
        # Global lifecycle hooks configuration
        lifecycleHooks:
          enabled: true
          schemaIsolation: true
          failOnError: true
        
        databasesUnderTest:
          # Snowflake with all features enabled
          - name: snowflake
            version: latest
            url: jdbc:snowflake://test.snowflakecomputing.com
            username: testuser
            password: testpass
            initScript: harness/init/snowflake/cloud-init.sql
            useSchemaIsolation: true
            
          # Postgres with schema isolation disabled
          - name: postgres
            version: 14
            url: jdbc:postgresql://localhost:5432/test
            username: postgres
            password: postgres
            useSchemaIsolation: false
            
          # H2 with minimal config
          - name: h2
            version: latest
            url: jdbc:h2:mem:test
        """
        
        when:
        def yaml = new Yaml()
        def config = yaml.loadAs(yamlContent, TestConfig.class)
        
        then:
        // Global lifecycle configuration
        config.lifecycleHooks.enabled == true
        config.lifecycleHooks.schemaIsolation == true
        config.lifecycleHooks.failOnError == true
        
        // Snowflake database configuration
        def snowflake = config.databasesUnderTest[0]
        snowflake.name == "snowflake"
        snowflake.useSchemaIsolation == true
        snowflake.initScript == "harness/init/snowflake/cloud-init.sql"
        
        // Postgres database configuration
        def postgres = config.databasesUnderTest[1]
        postgres.name == "postgres"
        postgres.useSchemaIsolation == false
        
        // H2 database configuration
        def h2 = config.databasesUnderTest[2]
        h2.name == "h2"
        h2.useSchemaIsolation == false  // default
        h2.initScript == null
    }
}