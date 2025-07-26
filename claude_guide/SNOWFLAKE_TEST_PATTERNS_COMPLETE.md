# Snowflake Test Harness Patterns - Complete Guide

This document provides the definitive patterns for creating Snowflake tests in the Liquibase Test Harness, based on our successful implementation work.

## Table of Contents
1. [Overview](#overview)
2. [File Structure](#file-structure)
3. [Changelog Pattern](#changelog-pattern)
4. [Expected SQL Pattern](#expected-sql-pattern)
5. [Expected Snapshot Pattern](#expected-snapshot-pattern)
6. [Running Tests](#running-tests)
7. [Common Issues and Solutions](#common-issues-and-solutions)

## Overview

The Liquibase Test Harness was originally designed for ephemeral Docker databases, but Snowflake (and other cloud databases) persist between test runs. This requires special patterns to ensure test isolation and repeatability.

### Key Principles
1. Each test must be completely self-contained
2. Tests must handle persistent database state
3. The init changeset with `runAlways="true"` ensures clean state
4. Expected SQL must include ALL generated SQL (including init changesets)
5. No SQL comments in init changesets (they break SQL comparison)

## File Structure

For each test, you need three files:

```
src/main/resources/liquibase/harness/change/
├── changelogs/snowflake/<testName>.xml      # The changelog with test changesets
├── expectedSql/snowflake/<testName>.sql     # Expected SQL output
└── expectedSnapshot/snowflake/<testName>.json # Expected database state
```

## Changelog Pattern

### Basic Structure

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns:snowflake="http://www.liquibase.org/xml/ns/snowflake"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd
                      http://www.liquibase.org/xml/ns/snowflake
                      http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd">

    <!-- CRITICAL: Init changeset for test isolation -->
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

    <!-- Your actual test changesets go here -->
    <changeSet id="actual-test" author="test-harness">
        <!-- Your test change type -->
    </changeSet>

</databaseChangeLog>
```

### Important Rules for Init Changeset

1. **NO SQL COMMENTS**: Never include SQL comments like `-- Drop and recreate schema`
2. **Use runAlways="true"**: Ensures clean state for every test run
3. **Use CONTINUE precondition**: Prevents test failure if check fails
4. **Include all tracking tables**: Both DATABASECHANGELOG and DATABASECHANGELOGLOCK
5. **Set LOCKED to TRUE**: The INSERT should use `VALUES (1, TRUE)`

### Example: Table Test

```xml
<changeSet id="reset-schema-createTable" author="test-harness" runAlways="true">
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

<changeSet id="createTable-test" author="test-harness">
    <createTable tableName="test_table">
        <column name="id" type="int">
            <constraints primaryKey="true" nullable="false"/>
        </column>
        <column name="name" type="varchar(100)">
            <constraints nullable="false"/>
        </column>
    </createTable>
    <rollback>
        <dropTable tableName="test_table"/>
    </rollback>
</changeSet>
```

### Example: Snowflake Extension Test

```xml
<changeSet id="reset-schema-createWarehouse" author="test-harness" runAlways="true">
    <preConditions onFail="CONTINUE">
        <sqlCheck expectedResult="1">SELECT 1</sqlCheck>
    </preConditions>
    <sql>
        DROP WAREHOUSE IF EXISTS LTHDB_TEST_WAREHOUSE;
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

## Expected SQL Pattern

The expected SQL file must include ALL SQL that will be generated, including the init changeset SQL.

### Structure

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

### Important Rules

1. **Include init SQL**: The `updateSql` command includes `runAlways="true"` changesets
2. **No comments**: Remove all SQL comments
3. **Exact formatting**: Match Snowflake's SQL output exactly
4. **Proper spacing**: Each statement on its own line(s)
5. **Schema qualification**: Include full schema paths (e.g., `LTHDB.TESTHARNESS.tablename`)

### Example: Complete Expected SQL

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
CREATE TABLE LTHDB.TESTHARNESS.test_table (id INT NOT NULL, name VARCHAR(100) NOT NULL, CONSTRAINT PK_TEST_TABLE PRIMARY KEY (id))
```

## Expected Snapshot Pattern

The snapshot captures the database state after changes are applied.

### Basic Structure

```json
{
  "snapshot": {
    "objects": {
      "liquibase.structure.core.Table": [
        {
          "table": {
            "name": "YOUR_TABLE_NAME",
            "schema": "TESTHARNESS"
          }
        }
      ],
      "liquibase.structure.core.Column": [
        {
          "column": {
            "name": "column_name",
            "type": {
              "typeName": "VARCHAR"
            },
            "nullable": false
          }
        }
      ]
    }
  }
}
```

### Example: Table with Columns

```json
{
  "snapshot": {
    "objects": {
      "liquibase.structure.core.Table": [
        {
          "table": {
            "name": "TEST_TABLE",
            "schema": "TESTHARNESS"
          }
        }
      ],
      "liquibase.structure.core.Column": [
        {
          "column": {
            "name": "ID",
            "nullable": false,
            "type": {
              "typeName": "NUMBER",
              "dataTypeId": "-5"
            }
          }
        },
        {
          "column": {
            "name": "NAME",
            "nullable": false,
            "type": {
              "typeName": "VARCHAR"
            }
          }
        }
      ],
      "liquibase.structure.core.PrimaryKey": [
        {
          "primaryKey": {
            "name": "PK_TEST_TABLE",
            "columns": [
              "ID"
            ]
          }
        }
      ]
    }
  }
}
```

## Running Tests

### With Pro License Key

```bash
# Set license key as environment variable
export LIQUIBASE_LICENSE_KEY="your-license-key-here"

# Run specific test
mvn test -Dtest=ChangeObjectTests -DchangeObjects=createWarehouse -DdbName=snowflake

# Run multiple tests
mvn test -Dtest=ChangeObjectTests -DchangeObjects=createWarehouse,alterWarehouse -DdbName=snowflake

# Clean and run (recommended for consistency)
mvn clean test -Dtest=ChangeObjectTests -DchangeObjects=createWarehouse -DdbName=snowflake
```

### Debugging Failed Tests

```bash
# Run with more output
mvn test -Dtest=ChangeObjectTests -DchangeObjects=createWarehouse -DdbName=snowflake -X

# Check generated vs expected SQL
mvn test -Dtest=ChangeObjectTests -DchangeObjects=createWarehouse -DdbName=snowflake 2>&1 | grep -A10 -B10 "Expected sql doesn't match"
```

## Common Issues and Solutions

### Issue 1: SQL Comparison Fails - Missing CREATE TABLE Statements

**Symptom**: Test fails with "Expected sql doesn't match generated sql" and generated SQL is missing init changeset SQL.

**Solution**: 
1. Ensure init changeset has `runAlways="true"`
2. Update expected SQL to include full init SQL
3. Remove any SQL comments from init changeset

### Issue 2: SQL Comparison Fails - Comments Breaking Parser

**Symptom**: Generated SQL is truncated or malformed when comments are present.

**Solution**: Remove ALL SQL comments from init changesets. Never use `--` comments.

### Issue 3: Snapshot Mismatch

**Symptom**: "Could not find match for element" in snapshot comparison.

**Solution**: 
1. Verify expected snapshot matches actual table structure
2. Check column names, types, and nullability
3. Ensure all created objects are included in snapshot

### Issue 4: "Change Type is not allowed without a valid Liquibase Pro License"

**Symptom**: Pro features fail validation.

**Solution**: Set LIQUIBASE_LICENSE_KEY environment variable before running tests.

### Issue 5: Test Pollution Between Runs

**Symptom**: Tests pass individually but fail when run together.

**Solution**: 
1. Ensure each test has proper init changeset
2. Drop ALL test objects in init (tables, sequences, warehouses, etc.)
3. Use unique object names per test

## Template for New Object Types

When adding support for new Snowflake object types, use this template:

### 1. Changelog Template

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
            DROP <OBJECT_TYPE> IF EXISTS LTHDB_TEST_<OBJECT>;
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

    <changeSet author="test-harness" id="create<ObjectType>-basic">
        <snowflake:create<ObjectType> <!-- attributes here --> />
        <rollback>
            <snowflake:drop<ObjectType> <!-- attributes here --> />
        </rollback>
    </changeSet>

</databaseChangeLog>
```

### 2. Create Expected SQL File

Include the full init SQL plus the expected SQL for your object creation.

### 3. Create Expected Snapshot

Include the appropriate object type in the snapshot structure.

## Best Practices

1. **Always clean first**: Run `mvn clean` before tests to ensure fresh state
2. **Test individually first**: Verify each test works alone before running batches
3. **Use descriptive IDs**: Help future debugging with clear changeset IDs
4. **Include rollback**: Always provide rollback for changesets
5. **Follow naming conventions**: Use LTHDB_ prefix for test objects
6. **Document special cases**: Add XML comments (not SQL!) explaining unusual patterns

## Conclusion

These patterns ensure reliable, repeatable tests for Snowflake in the Liquibase Test Harness. The key is understanding that cloud databases persist and require explicit cleanup, which is why the init changeset pattern is critical for test isolation.