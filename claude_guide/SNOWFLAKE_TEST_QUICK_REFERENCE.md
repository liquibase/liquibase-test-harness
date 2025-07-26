# Snowflake Test Harness - Quick Reference

## Essential Pattern

Every Snowflake test needs these three files:
```
changelogs/snowflake/<testName>.xml
expectedSql/snowflake/<testName>.sql  
expectedSnapshot/snowflake/<testName>.json
```

## Changelog MUST Have This Init Pattern

```xml
<changeSet id="reset-schema-<testName>" author="test-harness" runAlways="true">
    <preConditions onFail="CONTINUE">
        <sqlCheck expectedResult="1">SELECT 1</sqlCheck>
    </preConditions>
    <sql>
        DROP SCHEMA IF EXISTS TESTHARNESS CASCADE;
        CREATE SCHEMA TESTHARNESS;
        USE SCHEMA TESTHARNESS;
        CREATE TABLE DATABASECHANGELOG (
            ID VARCHAR(255) NOT NULL,
            AUTHOR VARCHAR(255) NOT NULL,
            FILENAME VARCHAR(255) NOT NULL,
            DATEEXECUTED TIMESTAMP NOT NULL,
            ORDEREXECUTED INT NOT NULL,
            EXECTYPE VARCHAR(10) NOT NULL,
            MD5SUM VARCHAR(35),
            DESCRIPTION VARCHAR(255),
            COMMENTS VARCHAR(255),
            TAG VARCHAR(255),
            LIQUIBASE VARCHAR(20),
            CONTEXTS VARCHAR(255),
            LABELS VARCHAR(255),
            DEPLOYMENT_ID VARCHAR(10)
        );
        CREATE TABLE DATABASECHANGELOGLOCK (
            ID INT NOT NULL,
            LOCKED BOOLEAN NOT NULL,
            LOCKGRANTED TIMESTAMP,
            LOCKEDBY VARCHAR(255),
            PRIMARY KEY (ID)
        );
        INSERT INTO DATABASECHANGELOGLOCK (ID, LOCKED) VALUES (1, TRUE);
    </sql>
    <rollback/>
</changeSet>
```

## Critical Rules

### ❌ NEVER DO THIS
```xml
<sql>
    -- Drop and recreate schema to ensure clean state  ❌ NO COMMENTS!
    DROP SCHEMA IF EXISTS TESTHARNESS CASCADE;
    -- Create tracking tables                          ❌ NO COMMENTS!
</sql>
```

### ✅ ALWAYS DO THIS
```xml
<sql>
    DROP SCHEMA IF EXISTS TESTHARNESS CASCADE;
    CREATE SCHEMA TESTHARNESS;
    USE SCHEMA TESTHARNESS;
</sql>
```

## Expected SQL Pattern

Expected SQL MUST include the init changeset SQL:

```sql
DROP SCHEMA IF EXISTS TESTHARNESS CASCADE
CREATE SCHEMA TESTHARNESS
USE SCHEMA TESTHARNESS
CREATE TABLE DATABASECHANGELOG (
ID VARCHAR(255) NOT NULL,
AUTHOR VARCHAR(255) NOT NULL,
FILENAME VARCHAR(255) NOT NULL,
DATEEXECUTED TIMESTAMP NOT NULL,
ORDEREXECUTED INT NOT NULL,
EXECTYPE VARCHAR(10) NOT NULL,
MD5SUM VARCHAR(35),
DESCRIPTION VARCHAR(255),
COMMENTS VARCHAR(255),
TAG VARCHAR(255),
LIQUIBASE VARCHAR(20),
CONTEXTS VARCHAR(255),
LABELS VARCHAR(255),
DEPLOYMENT_ID VARCHAR(10)
)
CREATE TABLE DATABASECHANGELOGLOCK (
ID INT NOT NULL,
LOCKED BOOLEAN NOT NULL,
LOCKGRANTED TIMESTAMP,
LOCKEDBY VARCHAR(255),
PRIMARY KEY (ID)
)
INSERT INTO DATABASECHANGELOGLOCK (ID, LOCKED) VALUES (1, TRUE)
[YOUR ACTUAL TEST SQL HERE]
```

## Running Tests

```bash
# With Pro license
export LIQUIBASE_LICENSE_KEY="your-key-here"

# Single test
mvn test -Dtest=ChangeObjectTests -DchangeObjects=createWarehouse -DdbName=snowflake

# Multiple tests  
mvn test -Dtest=ChangeObjectTests -DchangeObjects=createWarehouse,alterWarehouse -DdbName=snowflake

# Clean first (recommended)
mvn clean test -Dtest=ChangeObjectTests -DchangeObjects=createWarehouse -DdbName=snowflake
```

## Common Fixes

| Problem | Solution |
|---------|----------|
| SQL comparison fails | Remove SQL comments from init changeset |
| Missing CREATE TABLE in comparison | Expected SQL must include init changeset SQL |
| Pro license error | Set LIQUIBASE_LICENSE_KEY environment variable |
| Tests pollute each other | Ensure init changeset drops ALL test objects |
| Snapshot mismatch | Verify expected snapshot matches actual schema |

## Schema-Level vs Account-Level Objects

| Schema-Level Objects | Account-Level Objects |
|---------------------|---------------------|
| TABLE, VIEW, SEQUENCE | WAREHOUSE, DATABASE, SCHEMA |
| PROCEDURE, FUNCTION | ROLE, USER, RESOURCE MONITOR |
| STAGE, PIPE, STREAM, TASK | NETWORK POLICY, SHARE |
| Cleaned by DROP SCHEMA CASCADE | Must DROP explicitly in init |
| Simple names OK | Use LTHDB_TEST_ prefix |

## Copy-Paste Templates

### For Schema-Level Objects (Tables, Sequences, etc.)
```xml
<changeSet id="reset-schema-myTest" author="test-harness" runAlways="true">
    <preConditions onFail="CONTINUE">
        <sqlCheck expectedResult="1">SELECT 1</sqlCheck>
    </preConditions>
    <sql>
        DROP SCHEMA IF EXISTS TESTHARNESS CASCADE;
        CREATE SCHEMA TESTHARNESS;
        USE SCHEMA TESTHARNESS;
        CREATE TABLE DATABASECHANGELOG (
            ID VARCHAR(255) NOT NULL,
            AUTHOR VARCHAR(255) NOT NULL,
            FILENAME VARCHAR(255) NOT NULL,
            DATEEXECUTED TIMESTAMP NOT NULL,
            ORDEREXECUTED INT NOT NULL,
            EXECTYPE VARCHAR(10) NOT NULL,
            MD5SUM VARCHAR(35),
            DESCRIPTION VARCHAR(255),
            COMMENTS VARCHAR(255),
            TAG VARCHAR(255),
            LIQUIBASE VARCHAR(20),
            CONTEXTS VARCHAR(255),
            LABELS VARCHAR(255),
            DEPLOYMENT_ID VARCHAR(10)
        );
        CREATE TABLE DATABASECHANGELOGLOCK (
            ID INT NOT NULL,
            LOCKED BOOLEAN NOT NULL,
            LOCKGRANTED TIMESTAMP,
            LOCKEDBY VARCHAR(255),
            PRIMARY KEY (ID)
        );
        INSERT INTO DATABASECHANGELOGLOCK (ID, LOCKED) VALUES (1, TRUE);
    </sql>
    <rollback/>
</changeSet>
```

### For Account-Level Objects (Warehouses, Databases, etc.)
```xml
<changeSet id="reset-schema-myTest" author="test-harness" runAlways="true">
    <preConditions onFail="CONTINUE">
        <sqlCheck expectedResult="1">SELECT 1</sqlCheck>
    </preConditions>
    <sql>
        <!-- Drop account-level objects FIRST -->
        DROP WAREHOUSE IF EXISTS LTHDB_TEST_WAREHOUSE;
        DROP DATABASE IF EXISTS LTHDB_TEST_DATABASE;
        <!-- Add more account-level drops as needed -->
        
        <!-- Then standard schema cleanup -->
        DROP SCHEMA IF EXISTS TESTHARNESS CASCADE;
        CREATE SCHEMA TESTHARNESS;
        USE SCHEMA TESTHARNESS;
        <!-- ... tracking tables ... -->
    </sql>
    <rollback/>
</changeSet>
```

## Remember

1. **No SQL comments in init changesets** - They break SQL comparison
2. **Include all init SQL in expected files** - updateSql includes runAlways changesets  
3. **Drop everything in init** - Ensures clean state
4. **Use Pro license for Pro features** - Set environment variable
5. **Test individually first** - Easier debugging

This pattern works because it handles Snowflake's persistent state by explicitly cleaning up before each test run.