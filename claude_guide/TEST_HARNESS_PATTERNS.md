# Liquibase Test Harness Patterns

## Core Architecture Understanding

### The Test Harness Execution Flow
1. **Setup Phase**: 
   - Connects to database
   - Runs any init changesets (like our Snowflake init.xml)
   
2. **Test Execution**:
   - Generates SQL with `updateSql` command
   - Compares against expected SQL
   - Executes changes with `update` command
   - Takes database snapshot
   - Compares against expected snapshot
   
3. **Cleanup Phase**:
   - Uses `rollbackToDate` to revert changes
   - Relies on init.xml for persistent database cleanup

### Key Patterns Discovered

#### 1. The Ephemeral vs Persistent Database Pattern
**Problem**: Test harness was designed for Docker databases that reset between runs
**Solution**: For persistent databases, implement init.xml cleanup with `runAlways="true"`

#### 2. The Three-File Pattern
Every test MUST have:
```
changelogs/snowflake/testName.xml      # The actual test
expectedSql/snowflake/testName.sql     # What SQL should be generated
expectedSnapshot/snowflake/testName.json # Expected database state after execution
```

#### 3. The Self-Contained Test Pattern
Tests must not assume any database objects exist:
```xml
<!-- BAD: Assumes authors table exists -->
<changeSet id="1" author="test">
    <setTableRemarks tableName="authors" remarks="Test"/>
</changeSet>

<!-- GOOD: Creates what it needs -->
<changeSet id="setup" author="test">
    <createTable tableName="authors">...</createTable>
</changeSet>
<changeSet id="1" author="test">
    <setTableRemarks tableName="authors" remarks="Test"/>
</changeSet>
```

#### 4. The Complete Expected SQL Pattern
Expected SQL must include ALL operations:
```sql
-- BAD: Only includes the test operation
COMMENT ON TABLE authors IS 'Test Remark'

-- GOOD: Includes setup + test operation
CREATE TABLE LTHDB.TESTHARNESS.authors (...)
COMMENT ON TABLE LTHDB.TESTHARNESS.authors IS 'Test Remark'
```

#### 5. The Rollback Strategy Pattern
- Test harness uses `rollbackToDate` by default
- This rolls back to before test execution
- But init.xml with `runAlways="true"` persists
- Perfect for cleanup operations

## Database-Specific Patterns

### Snowflake
- Type conversions: `INTEGER` → `INT`
- No enforced foreign keys (by default)
- Minimum DATA_RETENTION_TIME_IN_DAYS = 1
- Requires explicit schema in expected SQL: `LTHDB.TESTHARNESS.tablename`

### Aurora PostgreSQL
- Uses `rollbackToDate` strategy
- Follows standard PostgreSQL patterns
- Less state persistence issues due to instance management

## Debugging Patterns

### When Tests Fail
1. Check for "previously run" errors → init.xml not running
2. Check for "table not found" → missing setup in test
3. Check for SQL mismatches → type conversion issues
4. Check for missing files → ensure all three files exist

### Common Fixes
- Add missing objects to init.xml cleanup
- Add table creation to test changesets
- Update expected SQL to match exact output
- Adjust expected snapshots for actual state