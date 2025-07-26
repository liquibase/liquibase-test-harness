# Hybrid Test Isolation Strategy

## Overview

Use **schema isolation** for schema-level objects and **unique naming** for account-level objects.

## Strategy by Object Type

### Schema-Isolated Tests (Recommended)
Use hardcoded test-specific schemas for:
- **Tables** → `TEST_CREATE_TABLE` schema
- **Sequences** → `TEST_CREATE_SEQUENCE` schema  
- **Views** → `TEST_CREATE_VIEW` schema
- **Procedures** → `TEST_CREATE_PROCEDURE` schema
- **Functions** → `TEST_CREATE_FUNCTION` schema

### Unique Naming Tests (Required)
Use unique object names in shared schema for:
- **Warehouses** → `CREATEWH_TEST_WAREHOUSE`, `ALTERWH_TEST_WAREHOUSE`
- **Databases** → `CREATEDB_TEST_DATABASE`, `ALTERDB_TEST_DATABASE`
- **Schemas** → `CREATESCHEMA_TEST_SCHEMA`
- **Roles** → `CREATEROLE_TEST_ROLE`

## Implementation Examples

### Schema-Isolated Test (createSequence)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog ...>
    
    <!-- Create isolated schema for this test -->
    <changeSet id="setup-schema" author="test-harness" runAlways="true">
        <sql>DROP SCHEMA IF EXISTS TEST_CREATE_SEQUENCE CASCADE</sql>
        <sql>CREATE SCHEMA TEST_CREATE_SEQUENCE</sql>
        <sql>USE SCHEMA TEST_CREATE_SEQUENCE</sql>
        <rollback/>
    </changeSet>
    
    <!-- Run test in isolated schema -->
    <changeSet id="test-createSequence" author="test-harness">
        <createSequence sequenceName="test_sequence"
                        startValue="1"
                        incrementBy="1"/>
        <rollback>
            <dropSequence sequenceName="test_sequence"/>
        </rollback>
    </changeSet>
    
    <!-- Cleanup -->
    <changeSet id="cleanup-schema" author="test-harness" runAlways="true">
        <sql>USE SCHEMA TESTHARNESS</sql>
        <sql>DROP SCHEMA IF EXISTS TEST_CREATE_SEQUENCE CASCADE</sql>
        <rollback/>
    </changeSet>
    
</databaseChangeLog>
```

### Unique Naming Test (createWarehouse)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog ...>
    
    <!-- Pre-test cleanup with unique names -->
    <changeSet id="cleanup" author="test-harness" runAlways="true">
        <preConditions onFail="CONTINUE">
            <sqlCheck expectedResult="0">SELECT 1 WHERE 1=0</sqlCheck>
        </preConditions>
        <sql>DROP WAREHOUSE IF EXISTS CREATEWH_TEST_WAREHOUSE</sql>
        <sql>DROP WAREHOUSE IF EXISTS CREATEWH_MULTICLUSTER_WH</sql>
        <rollback/>
    </changeSet>
    
    <!-- Test with unique names -->
    <changeSet id="test-createWarehouse" author="test-harness">
        <snowflake:createWarehouse warehouseName="CREATEWH_TEST_WAREHOUSE"
                                  warehouseSize="XSMALL"/>
        <rollback>
            <snowflake:dropWarehouse warehouseName="CREATEWH_TEST_WAREHOUSE"/>
        </rollback>
    </changeSet>
    
</databaseChangeLog>
```

## Schema Naming Convention

Each test gets its own schema named: `TEST_<OPERATION>_<OBJECT>`

Examples:
- `TEST_CREATE_SEQUENCE`
- `TEST_ALTER_SEQUENCE`
- `TEST_CREATE_TABLE`
- `TEST_MODIFY_DATA_TYPE`
- `TEST_ADD_FOREIGN_KEY`

## Benefits

1. **Maximum Isolation** - Schema-level objects are completely isolated
2. **Parallel Execution** - Tests can run simultaneously without conflicts
3. **Clean State** - Each test starts fresh with its own schema
4. **Account Objects Handled** - Unique naming prevents warehouse/database conflicts
5. **Test Independence** - No shared state between tests

## Migration Plan

### Phase 1: Schema-Level Objects
Convert these tests to use isolated schemas:
- [ ] createSequence, alterSequence, dropSequence, renameSequence
- [ ] createTable, alterTable, dropTable, renameTable
- [ ] createView, alterView, dropView
- [ ] createProcedure, alterProcedure, dropProcedure
- [ ] createFunction, dropFunction

### Phase 2: Account-Level Objects  
Update these tests to use unique naming:
- [ ] createWarehouse, alterWarehouse, dropWarehouse
- [ ] createDatabase, alterDatabase, dropDatabase
- [ ] createSchema (special case - creates schemas with unique names)

### Phase 3: Update Expected Files
- [ ] Update expectedSql files to match new schema names
- [ ] Update expectedSnapshot files if needed

## Special Considerations

### createSchema Test
This test creates schemas, so it can't use schema isolation. Use unique names:
```xml
<changeSet id="test-createSchema">
    <sql>CREATE SCHEMA CREATESCHEMA_TEST_SCHEMA</sql>
    <rollback>
        <sql>DROP SCHEMA IF EXISTS CREATESCHEMA_TEST_SCHEMA CASCADE</sql>
    </rollback>
</changeSet>
```

### Cross-Schema References
If tests need foreign keys or views across schemas, they'll need special handling.

### Warehouse Usage in Schema Tests
Even in isolated schemas, queries need an active warehouse. Ensure the default warehouse is available.