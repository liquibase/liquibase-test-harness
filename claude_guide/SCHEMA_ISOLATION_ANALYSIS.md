# Schema Isolation Analysis for Test Harness

## Summary

After testing schema isolation for parallel test execution, we discovered that while the approach is elegant in theory, it has practical limitations in the current test harness architecture.

## What Works

### Schema-Level Objects
For objects that exist within schemas (tables, views, sequences, procedures, functions), schema isolation works well:

```xml
<!-- Create test-specific schema -->
<changeSet id="setup-schema" runAlways="true">
    <sql>DROP SCHEMA IF EXISTS TEST_CREATE_TABLE CASCADE</sql>
    <sql>CREATE SCHEMA TEST_CREATE_TABLE</sql>
    <sql>USE SCHEMA TEST_CREATE_TABLE</sql>
</changeSet>

<!-- Run test in isolated schema -->
<changeSet id="test">
    <createTable tableName="authors">
        <column name="id" type="int"/>
    </createTable>
</changeSet>

<!-- Cleanup -->
<changeSet id="cleanup-schema" runAlways="true">
    <sql>USE SCHEMA TESTHARNESS</sql>
    <sql>DROP SCHEMA IF EXISTS TEST_CREATE_TABLE CASCADE</sql>
</changeSet>
```

## What Doesn't Work

### Account-Level Objects
1. **Warehouses** - These are account-level objects, not schema-level
2. **Databases** - Also account-level
3. **Roles/Users** - Security objects are account-level

### Test Harness Limitations
1. **Expected SQL Validation** - The test harness expects exact SQL matches, including schema names
2. **Snapshot Comparison** - Snapshots expect objects in TESTHARNESS schema
3. **Suspended Warehouse Issue** - Creating a suspended warehouse then trying to use it causes lock issues

## Recommended Approach: Unique Naming

Instead of schema isolation, use unique object names within the shared TESTHARNESS schema:

```xml
<!-- Pre-test cleanup with unique names -->
<changeSet id="cleanup" runAlways="true">
    <sql>DROP TABLE IF EXISTS TABLE_${testName}_${timestamp}</sql>
    <sql>DROP SEQUENCE IF EXISTS SEQ_${testName}_${timestamp}</sql>
</changeSet>

<!-- Test with unique names -->
<changeSet id="test">
    <createTable tableName="TABLE_${testName}_${timestamp}">
        <column name="id" type="int"/>
    </createTable>
</changeSet>
```

## Parallel Execution Strategy

For true parallel execution:

1. **Use unique object names** - Prefix with test name and timestamp/UUID
2. **Clean before each test** - DROP IF EXISTS in runAlways changesets
3. **Avoid shared state** - Don't depend on objects from other tests
4. **Handle account-level objects carefully** - Warehouses, databases need special handling

## Future Enhancement Ideas

1. **Test Harness Enhancement** - Add schema isolation mode to the framework
2. **Dynamic Expected SQL** - Support property substitution in expected files
3. **Scoped Snapshots** - Compare snapshots within test-specific schemas
4. **Resource Pools** - Pre-create pools of warehouses for tests to use

## Conclusion

While schema isolation is technically feasible for schema-level objects, the current test harness architecture and Snowflake's object hierarchy make unique naming within a shared schema the more practical approach for achieving test independence and parallel execution.