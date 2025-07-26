# Test Independence Strategy for Snowflake Test Harness

## Problem Statement
Tests currently fail when run in different orders or combinations because:
- Objects persist between test runs
- DATABASECHANGELOG tracking causes "previously run" errors
- Tests assume clean state but don't enforce it
- Cleanup can fail, leaving database dirty

## Solution Strategies

### 1. Self-Contained Tests (Recommended)
Make each test completely independent by:

#### A. Pre-Test Cleanup Pattern
Each test should start with cleanup of its own objects:

```xml
<changeSet id="cleanup-before-test" author="test-harness" runAlways="true">
    <preConditions onFail="CONTINUE">
        <sqlCheck expectedResult="1">SELECT 1</sqlCheck>
    </preConditions>
    <!-- Drop any objects this test will create -->
    <sql>DROP WAREHOUSE IF EXISTS TEST_WAREHOUSE_${testId}</sql>
    <rollback/>
</changeSet>

<changeSet id="actual-test" author="test-harness">
    <snowflake:createWarehouse warehouseName="TEST_WAREHOUSE_${testId}"/>
    <rollback>
        <snowflake:dropWarehouse warehouseName="TEST_WAREHOUSE_${testId}"/>
    </rollback>
</changeSet>
```

#### B. Unique Object Names
Use timestamps or UUIDs in object names:
- Instead of: `LTHDB_TEST_WAREHOUSE`
- Use: `LTHDB_TEST_WAREHOUSE_${timestamp}`

#### C. Test-Specific Schemas
Each test could create its own schema:
```xml
<changeSet id="create-test-schema" author="test-harness">
    <sql>CREATE SCHEMA IF NOT EXISTS TEST_${testName}_SCHEMA</sql>
    <sql>USE SCHEMA TEST_${testName}_SCHEMA</sql>
</changeSet>
```

### 2. Enhanced Init Pattern
Improve 00_init.xml to be more comprehensive:

```xml
<changeSet id="ensure-clean-state" author="test-harness" runAlways="true">
    <!-- Clean tracking tables -->
    <sql>DELETE FROM DATABASECHANGELOG WHERE FILENAME NOT LIKE '%00_init.xml'</sql>
    <sql>UPDATE DATABASECHANGELOGLOCK SET LOCKED = FALSE WHERE ID = 1</sql>
    
    <!-- Clean ALL known test objects -->
    <sql>
        DECLARE
            warehouse_name STRING;
            c1 CURSOR FOR SELECT warehouse_name FROM INFORMATION_SCHEMA.WAREHOUSES 
                         WHERE warehouse_name LIKE 'LTHDB_TEST_%';
        BEGIN
            FOR record IN c1 DO
                EXECUTE IMMEDIATE 'DROP WAREHOUSE IF EXISTS ' || record.warehouse_name;
            END FOR;
        END;
    </sql>
</changeSet>
```

### 3. Test Harness Framework Enhancements

#### A. Pre-Test Hook
Add a pre-test cleanup phase to the test harness:

```groovy
// In ChangeObjectTests.groovy
def setup() {
    // Run cleanup before EVERY test
    runCleanupChangelog()
}

private void runCleanupChangelog() {
    // Execute 00_init.xml before each test
    def cleanupChangelog = "liquibase/harness/change/changelogs/snowflake/00_init.xml"
    liquibase.update(cleanupChangelog)
}
```

#### B. Test Isolation Mode
Add a test parameter for isolation level:

```bash
# Full isolation - runs init before each test
mvn test -DdbName=snowflake -DisolationMode=full

# Shared mode - current behavior
mvn test -DdbName=snowflake -DisolationMode=shared

# Smart mode - detects conflicts and runs init when needed
mvn test -DdbName=snowflake -DisolationMode=smart
```

### 4. Practical Implementation Steps

For immediate improvement without framework changes:

1. **Update all Snowflake tests** to include pre-cleanup:
   ```xml
   <!-- At the start of each test changelog -->
   <include file="00_init.xml" relativeToChangelogFile="true"/>
   ```

2. **Use unique identifiers** for all objects:
   ```xml
   <property name="testId" value="${changeSet.id}"/>
   <snowflake:createWarehouse warehouseName="WAREHOUSE_${testId}"/>
   ```

3. **Add existence checks** before creates:
   ```xml
   <preConditions onFail="MARK_RAN">
       <not>
           <sqlCheck expectedResult="1">
               SELECT COUNT(*) FROM INFORMATION_SCHEMA.WAREHOUSES 
               WHERE warehouse_name = 'TEST_WAREHOUSE'
           </sqlCheck>
       </not>
   </preConditions>
   ```

## Recommendation

**Short term**: Update each test to be self-contained with:
- Pre-test cleanup changesets
- Unique object names using timestamps
- Explicit cleanup in rollback blocks

**Long term**: Enhance the test harness framework to:
- Run init before each test automatically
- Support isolated test schemas
- Provide better test lifecycle hooks

This ensures tests work correctly whether run:
- Individually: Each test cleans up before running
- In groups: No conflicts due to unique names
- All together: 00_init runs first, plus each test self-cleans