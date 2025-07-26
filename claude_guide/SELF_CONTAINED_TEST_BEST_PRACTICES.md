# Self-Contained Test Best Practices

## Overview

To ensure tests can run independently and in parallel, each test should be completely self-contained with unique object names and proper cleanup.

## Best Practices

### 1. Use Unique Object Names

```xml
<!-- Use test name as prefix for all objects -->
<changeSet id="test-createSequence">
    <createSequence sequenceName="CREATESEQ_TEST_SEQUENCE"/>
</changeSet>

<changeSet id="test-alterSequence">
    <createSequence sequenceName="ALTERSEQ_TEST_SEQUENCE"/>
    <alterSequence sequenceName="ALTERSEQ_TEST_SEQUENCE" incrementBy="5"/>
</changeSet>
```

### 2. Pre-Test Cleanup Pattern

```xml
<!-- Always clean up before test runs -->
<changeSet id="cleanup-${testName}" runAlways="true">
    <preConditions onFail="CONTINUE">
        <sqlCheck expectedResult="0">SELECT 1 WHERE 1=0</sqlCheck>
    </preConditions>
    <sql>DROP SEQUENCE IF EXISTS ${testName}_TEST_SEQUENCE</sql>
    <sql>DROP TABLE IF EXISTS ${testName}_AUTHORS</sql>
    <rollback/>
</changeSet>
```

### 3. Self-Contained Setup

```xml
<!-- Don't assume objects exist - create what you need -->
<changeSet id="setup-${testName}">
    <createTable tableName="${testName}_AUTHORS">
        <column name="id" type="int">
            <constraints primaryKey="true"/>
        </column>
        <column name="name" type="varchar(100)"/>
    </createTable>
</changeSet>

<!-- Then test your feature -->
<changeSet id="test-${testName}">
    <addColumn tableName="${testName}_AUTHORS">
        <column name="email" type="varchar(200)"/>
    </addColumn>
</changeSet>
```

### 4. Proper Rollback

```xml
<changeSet id="test-${testName}">
    <createWarehouse warehouseName="${testName}_WAREHOUSE"/>
    <rollback>
        <!-- Ensure cleanup even if test fails -->
        <snowflake:dropWarehouse warehouseName="${testName}_WAREHOUSE"/>
    </rollback>
</changeSet>
```

## Naming Conventions

### Object Prefixes by Test Type
- `CREATESEQ_` - createSequence tests
- `ALTERSEQ_` - alterSequence tests  
- `CREATETBL_` - createTable tests
- `CREATEWH_` - createWarehouse tests
- `CREATEDB_` - createDatabase tests

### Examples
- `CREATESEQ_TEST_SEQUENCE`
- `ALTERSEQ_ORDER_SEQUENCE`
- `CREATETBL_AUTHORS`
- `CREATEWH_TEST_WAREHOUSE`

## Template for New Tests

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns:snowflake="http://www.liquibase.org/xml/ns/snowflake"
                   xsi:schemaLocation="...">

    <!-- Step 1: Pre-test cleanup -->
    <changeSet id="cleanup-testName" author="test-harness" runAlways="true">
        <preConditions onFail="CONTINUE">
            <sqlCheck expectedResult="0">SELECT 1 WHERE 1=0</sqlCheck>
        </preConditions>
        <sql>DROP [OBJECT_TYPE] IF EXISTS TESTNAME_[OBJECT]</sql>
        <rollback/>
    </changeSet>

    <!-- Step 2: Setup (if needed) -->
    <changeSet id="setup-testName" author="test-harness">
        <!-- Create any prerequisites -->
    </changeSet>

    <!-- Step 3: The actual test -->
    <changeSet id="test-testName" author="test-harness">
        <!-- Your test change here -->
        <rollback>
            <!-- Cleanup for this specific change -->
        </rollback>
    </changeSet>

</databaseChangeLog>
```

## Benefits

1. **Parallel Execution** - No naming conflicts between tests
2. **Test Independence** - Each test sets up its own environment
3. **Predictable State** - Tests always start clean
4. **Easy Debugging** - Object names indicate which test created them
5. **No Cleanup Dependencies** - Each test cleans up after itself

## Gotchas to Avoid

1. **Don't use generic names** like `test_table` or `test_sequence`
2. **Don't assume cleanup from other tests** - Always clean your own objects
3. **Don't create suspended warehouses** without resuming them
4. **Don't forget rollback blocks** - They ensure cleanup on failure
5. **Don't share objects between changesets** in different test files

## Migration Strategy

To migrate existing tests:
1. Add unique prefix to all object names
2. Add pre-test cleanup changeset
3. Update expected SQL files with new names
4. Test individually then in groups