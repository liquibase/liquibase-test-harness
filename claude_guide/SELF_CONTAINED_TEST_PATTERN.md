# Self-Contained Test Pattern for Snowflake Test Harness

## The Solution: Drop and Recreate Schema

Each test should drop and recreate the TESTHARNESS schema to ensure complete isolation.

## Why This Works

1. **Removes ALL objects** - Tables, sequences, views, procedures, etc.
2. **Removes tracking tables** - DATABASECHANGELOG and DATABASECHANGELOGLOCK
3. **Fresh start every time** - No "previously run" errors
4. **No state pollution** - Tests can't interfere with each other
5. **Enables parallel execution** - Each test gets its own clean schema

## Implementation Pattern

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog ...>
    
    <!-- Step 1: Drop and recreate schema -->
    <changeSet id="reset-schema-${testName}" author="test-harness" runAlways="true">
        <preConditions onFail="CONTINUE">
            <sqlCheck expectedResult="1">SELECT 1</sqlCheck>
        </preConditions>
        <sql>
            DROP SCHEMA IF EXISTS TESTHARNESS CASCADE;
            CREATE SCHEMA TESTHARNESS;
            USE SCHEMA TESTHARNESS;
        </sql>
        <rollback/>
    </changeSet>
    
    <!-- Step 2: Your test changesets -->
    <changeSet id="test" author="original-author">
        <!-- Keep original changeset content -->
        <createSequence sequenceName="test_sequence"/>
    </changeSet>
    
</databaseChangeLog>
```

## Benefits

1. **No naming conflicts** - Clean schema each time
2. **No tracking issues** - Fresh DATABASECHANGELOG
3. **Parallel safe** - Tests are completely isolated
4. **Simple pattern** - Just add the reset changeset
5. **Original IDs preserved** - No need to modify existing changesets

## Expected SQL Pattern

Your expectedSql files should include the schema operations:

```sql
DROP SCHEMA IF EXISTS TESTHARNESS CASCADE
CREATE SCHEMA TESTHARNESS
USE SCHEMA TESTHARNESS
CREATE SEQUENCE TESTHARNESS.test_sequence START WITH 1 INCREMENT BY 5
```

## Migration Steps

For each test:
1. Add the reset-schema changeset at the beginning
2. Update expectedSql to include schema operations
3. Keep all other changesets unchanged
4. Test individually to verify

## Example Tests to Convert

- createSequence ✓
- alterSequence
- dropSequence
- createTable
- alterTable
- createWarehouse (warehouses are account-level, still exist after schema drop)
- etc.

## Special Cases

### Warehouses and Databases
These are account-level objects and survive schema drops. They still need cleanup:

```xml
<changeSet id="cleanup-warehouses" runAlways="true">
    <sql>DROP WAREHOUSE IF EXISTS TEST_WAREHOUSE</sql>
    <sql>DROP DATABASE IF EXISTS TEST_DATABASE</sql>
</changeSet>
```

### Cross-Schema Dependencies
If tests need cross-schema references, they won't work with this pattern. Consider using unique naming instead.

## Conclusion

Drop and recreate schema is the simplest, most reliable way to ensure test isolation in the Snowflake test harness.