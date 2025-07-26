# Snowflake Objects: Schema-Level vs Account-Level Testing Patterns

## Overview

Snowflake has two distinct types of objects that require different testing patterns:

1. **Schema-Level Objects** - Exist within a schema (tables, views, sequences, procedures, functions, etc.)
2. **Account-Level Objects** - Exist at the account level (warehouses, databases, schemas, roles, users, etc.)

## Schema-Level Objects Pattern

These objects exist within the TESTHARNESS schema and are cleaned up when we DROP SCHEMA CASCADE.

### Examples
- TABLE
- VIEW
- SEQUENCE
- PROCEDURE
- FUNCTION
- STAGE (when created in a schema)
- PIPE
- STREAM
- TASK

### Init Pattern for Schema-Level Objects

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

### Example: Create Table Test

```xml
<changeSet id="createTable-test" author="test-harness">
    <createTable tableName="test_table">
        <column name="id" type="int">
            <constraints primaryKey="true"/>
        </column>
    </createTable>
</changeSet>
```

## Account-Level Objects Pattern

These objects exist outside any schema and must be explicitly dropped in the init changeset.

### Examples
- WAREHOUSE
- DATABASE
- SCHEMA
- ROLE
- USER
- RESOURCE MONITOR
- NETWORK POLICY
- SHARE

### Init Pattern for Account-Level Objects

```xml
<changeSet id="reset-schema-<testName>" author="test-harness" runAlways="true">
    <preConditions onFail="CONTINUE">
        <sqlCheck expectedResult="1">SELECT 1</sqlCheck>
    </preConditions>
    <sql>
        <!-- CRITICAL: Drop account-level objects FIRST -->
        DROP WAREHOUSE IF EXISTS LTHDB_TEST_<OBJECT_NAME>;
        DROP DATABASE IF EXISTS LTHDB_TEST_<OBJECT_NAME>;
        <!-- Add more account-level drops as needed -->
        
        <!-- Then do standard schema cleanup -->
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

### Example: Warehouse Test

```xml
<changeSet id="reset-schema-createWarehouse" author="test-harness" runAlways="true">
    <preConditions onFail="CONTINUE">
        <sqlCheck expectedResult="1">SELECT 1</sqlCheck>
    </preConditions>
    <sql>
        <!-- Drop test warehouses first -->
        DROP WAREHOUSE IF EXISTS LTHDB_TEST_WAREHOUSE;
        DROP WAREHOUSE IF EXISTS LTHDB_TEST_MULTICLUSTER_WH;
        
        <!-- Standard schema cleanup -->
        DROP SCHEMA IF EXISTS TESTHARNESS CASCADE;
        CREATE SCHEMA TESTHARNESS;
        USE SCHEMA TESTHARNESS;
        <!-- ... tracking tables ... -->
    </sql>
    <rollback/>
</changeSet>

<changeSet author="test-harness" id="createWarehouse-basic">
    <snowflake:createWarehouse warehouseName="LTHDB_TEST_WAREHOUSE"
                    warehouseSize="XSMALL"
                    autoSuspend="60"
                    autoResume="true"
                    comment="Test warehouse"/>
    <rollback>
        <snowflake:dropWarehouse warehouseName="LTHDB_TEST_WAREHOUSE"/>
    </rollback>
</changeSet>
```

### Example: Database Test

```xml
<changeSet id="reset-schema-createDatabase" author="test-harness" runAlways="true">
    <preConditions onFail="CONTINUE">
        <sqlCheck expectedResult="1">SELECT 1</sqlCheck>
    </preConditions>
    <sql>
        <!-- Drop test databases first -->
        DROP DATABASE IF EXISTS LTHDB_TEST_DATABASE;
        DROP DATABASE IF EXISTS LTHDB_TEST_TRANSIENT_DB;
        DROP DATABASE IF EXISTS LTHDB_TEST_RETENTION_DB;
        
        <!-- Standard schema cleanup -->
        DROP SCHEMA IF EXISTS TESTHARNESS CASCADE;
        CREATE SCHEMA TESTHARNESS;
        USE SCHEMA TESTHARNESS;
        <!-- ... tracking tables ... -->
    </sql>
    <rollback/>
</changeSet>

<changeSet author="test-harness" id="createDatabase-basic">
    <snowflake:createDatabase databaseName="LTHDB_TEST_DATABASE"
                   comment="Basic test database"/>
    <rollback>
        <snowflake:dropDatabase databaseName="LTHDB_TEST_DATABASE"/>
    </rollback>
</changeSet>
```

## Mixed Object Tests

Some tests might create both schema-level and account-level objects. In these cases:

1. Drop account-level objects first
2. Then drop schema
3. Order matters to avoid dependency issues

### Example: Schema Creation Test

```xml
<changeSet id="reset-schema-createSchema" author="test-harness" runAlways="true">
    <preConditions onFail="CONTINUE">
        <sqlCheck expectedResult="1">SELECT 1</sqlCheck>
    </preConditions>
    <sql>
        <!-- Drop test schemas (account-level) -->
        DROP SCHEMA IF EXISTS LTHDB_TEST_SCHEMA CASCADE;
        DROP SCHEMA IF EXISTS LTHDB_TEST_TRANSIENT_SCHEMA CASCADE;
        
        <!-- Standard TESTHARNESS schema cleanup -->
        DROP SCHEMA IF EXISTS TESTHARNESS CASCADE;
        CREATE SCHEMA TESTHARNESS;
        USE SCHEMA TESTHARNESS;
        <!-- ... tracking tables ... -->
    </sql>
    <rollback/>
</changeSet>
```

## Key Differences Summary

| Aspect | Schema-Level Objects | Account-Level Objects |
|--------|---------------------|----------------------|
| Location | Inside a schema | Outside schemas |
| Cleanup | Automatic with DROP SCHEMA CASCADE | Must explicitly DROP each |
| Naming | Can use simple names | Should use LTHDB_TEST_ prefix |
| Examples | Tables, Views, Sequences | Warehouses, Databases, Schemas |
| DROP placement | Not needed (CASCADE handles it) | Before schema drop in init |

## Best Practices

### For Schema-Level Objects
1. Let DROP SCHEMA CASCADE handle cleanup
2. Focus on creating self-contained tests within TESTHARNESS schema
3. Use simple, descriptive names

### For Account-Level Objects
1. **Always use LTHDB_TEST_ prefix** to avoid conflicts
2. **Explicitly DROP in init changeset** before schema operations
3. **List all test objects** that will be created
4. **Order drops correctly** to handle dependencies

### Naming Conventions
```
Schema-level: test_table, test_sequence, test_function
Account-level: LTHDB_TEST_WAREHOUSE, LTHDB_TEST_DATABASE, LTHDB_TEST_ROLE
```

## Template for New Account-Level Object Type

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns:snowflake="http://www.liquibase.org/xml/ns/snowflake"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd
                      http://www.liquibase.org/xml/ns/snowflake
                      http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd">

    <changeSet id="reset-schema-create<ObjectType>" author="test-harness" runAlways="true">
        <preConditions onFail="CONTINUE">
            <sqlCheck expectedResult="1">SELECT 1</sqlCheck>
        </preConditions>
        <sql>
            <!-- Drop all account-level objects this test will create -->
            DROP <OBJECT_TYPE> IF EXISTS LTHDB_TEST_<NAME1>;
            DROP <OBJECT_TYPE> IF EXISTS LTHDB_TEST_<NAME2>;
            <!-- Add more as needed -->
            
            <!-- Standard schema cleanup -->
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

    <!-- Test changesets -->
    <changeSet author="test-harness" id="create<ObjectType>-test">
        <snowflake:create<ObjectType> name="LTHDB_TEST_<NAME>" />
        <rollback>
            <snowflake:drop<ObjectType> name="LTHDB_TEST_<NAME>" />
        </rollback>
    </changeSet>

</databaseChangeLog>
```

## Conclusion

Understanding whether an object is schema-level or account-level is critical for writing correct test cleanup. Schema-level objects get cleaned up automatically with DROP SCHEMA CASCADE, while account-level objects must be explicitly dropped. This distinction ensures tests are truly isolated and repeatable.