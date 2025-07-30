# Test Lifecycle Hooks

## Overview

The Liquibase Test Harness now supports lifecycle hooks that allow you to run initialization and cleanup scripts before and after each test. This is particularly useful for cloud databases like Snowflake that persist between test runs.

## Features

- **Pre-test hooks**: Run SQL scripts or changelogs before each test
- **Post-test hooks**: Run cleanup scripts after each test
- **Database-specific scripts**: Scripts can be specific to database type and version
- **Test-specific scripts**: Scripts can be specific to individual tests
- **Schema isolation**: Create isolated schemas per test for cloud databases
- **Graceful failure handling**: Hooks fail gracefully without breaking existing tests
- **Backward compatible**: Disabled by default to maintain compatibility

## Implementation Status

✅ Core infrastructure implemented:
- `TestLifecycleHook` interface
- `TestContext` for passing test information
- `ScriptBasedLifecycleHook` for SQL script execution
- `SchemaIsolationHook` for per-test schema isolation
- `TestLifecycleManager` singleton for managing hooks
- Integration with `ChangeObjectTests`

✅ Snowflake scripts created:
- `init.sql` - Clears DATABASECHANGELOG entries before tests
- `cleanup.sql` - Cleans up after tests

✅ Schema isolation implemented:
- Creates unique `TEST_<TESTNAME>` schemas per test
- Automatic cleanup after test completion
- Configuration-based enablement per database

## Usage

### Enabling Lifecycle Hooks

Lifecycle hooks are disabled by default. To enable them:

1. **Via System Property**:
   ```bash
   mvn test -Dliquibase.harness.lifecycle.enabled=true
   ```

2. **Via Configuration**:
   ```yaml
   # harness-config.yml
   lifecycleHooks:
     enabled: true
   ```

### Enabling Schema Isolation

Schema isolation creates a unique schema for each test to prevent interference between tests on cloud databases:

```yaml
# harness-config.yml
lifecycleHooks:
  enabled: true

databasesUnderTest:
  - name: snowflake
    useSchemaIsolation: true
    # ... other database config
```

When enabled, each test will:
1. Create a schema named `TEST_<TESTNAME>` (e.g., `TEST_CREATETABLE`)
2. Run all test operations in that isolated schema
3. Clean up the schema after test completion

### Adding Scripts

Place SQL scripts in the following directory structure:
```
src/main/resources/liquibase/harness/lifecycle/
├── {database}/
│   ├── init.sql      # Runs before each test
│   └── cleanup.sql   # Runs after each test
└── {database}/{version}/
    ├── init.sql      # Version-specific pre-test script
    └── cleanup.sql   # Version-specific post-test script
```

Test-specific scripts:
```
src/test/resources/harness/changeObjects/
└── {database}/
    ├── {testName}.init.sql    # Test-specific initialization
    └── {testName}.cleanup.sql # Test-specific cleanup
```

Example for Snowflake:
```
src/main/resources/liquibase/harness/lifecycle/
└── snowflake/
    ├── suite-init.sql  # One-time setup (run manually)
    ├── init.sql        # Before each test
    ├── cleanup.sql     # After each test
    └── README.md       # Snowflake-specific documentation

src/test/resources/harness/changeObjects/
└── snowflake/
    ├── createTable.init.sql    # Setup for createTable test
    └── createTable.cleanup.sql # Cleanup for createTable test
```

**Note**: The `suite-init.sql` is NOT automatically executed. It must be run manually before testing.

### Script Examples

**init.sql** (Snowflake):
```sql
-- Ensure DATABASECHANGELOGLOCK is unlocked
UPDATE DATABASECHANGELOGLOCK SET LOCKED = FALSE WHERE ID = 1;

-- Clean any previous test runs (except init.xml)
DELETE FROM DATABASECHANGELOG WHERE FILENAME NOT LIKE '%init.xml';
```

**cleanup.sql** (Snowflake):
```sql
-- Clear DATABASECHANGELOG entries (except for init.xml)
DELETE FROM DATABASECHANGELOG WHERE FILENAME NOT LIKE '%init.xml';

-- Ensure DATABASECHANGELOGLOCK is released
UPDATE DATABASECHANGELOGLOCK SET LOCKED = FALSE WHERE ID = 1;
```

## Debugging

Enable debug logging to see what's happening:
```bash
mvn test -Dliquibase.harness.lifecycle.enabled=true \
         -Dliquibase.harness.lifecycle.debug=true
```

## Extending

You can create custom lifecycle hooks by:

1. Implementing the `TestLifecycleHook` interface
2. Registering via ServiceLoader (create `META-INF/services/liquibase.harness.lifecycle.TestLifecycleHook`)

Example:
```groovy
class MyCustomHook implements TestLifecycleHook {
    void beforeTest(TestContext context) {
        // Custom pre-test logic
    }
    
    void afterTest(TestContext context) {
        // Custom post-test logic
    }
    
    boolean supports(DatabaseUnderTest database) {
        return database.name == "mydb"
    }
}
```

## Future Enhancements

- [ ] Support for Liquibase changelogs (not just SQL scripts)
- [ ] Configuration file support (harness-config.yml)
- [ ] More database-specific examples
- [ ] Test-specific hooks
- [ ] Performance metrics collection

## Schema Isolation Details

The `SchemaIsolationHook` provides test isolation for cloud databases:

### How it works:
1. Before each test, creates a schema named `TEST_<SANITIZED_TEST_NAME>`
2. Sets the default schema for the test to use the isolated schema
3. All Liquibase operations run in the isolated schema
4. After test completion, drops the isolated schema

### Benefits:
- **Parallel test execution**: Tests can run simultaneously without conflicts
- **Clean state**: Each test starts with a fresh schema
- **Debugging**: Failed test schemas can be preserved for investigation
- **Cloud database support**: Essential for databases that persist between runs

### Configuration:
```yaml
databasesUnderTest:
  - name: snowflake
    useSchemaIsolation: true
    # Original schema restored after test
```

### Important Notes:
- Test names are sanitized (special characters replaced with underscores)
- Expected SQL files must use the isolated schema name (e.g., `TEST_CREATETABLE`)
- The original schema is restored before dropping the test schema
- Currently only implemented for Snowflake, but extensible to other databases

## Notes

- Scripts are executed with the same database connection used by the tests
- Scripts should be idempotent - they may run multiple times
- Keep scripts simple and focused on cleanup/initialization
- Test the scripts manually first before enabling hooks
- Schema isolation requires updating expected SQL files to use the test schema name