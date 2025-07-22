# Snowflake Test Harness Strategy

## Overview
This document outlines the comprehensive testing strategy for Snowflake in the Liquibase Test Harness, incorporating lessons learned from Aurora's implementation and our critical discovery about persistent database state management.

## Key Discoveries

### The Persistent Database Problem
Unlike Docker-based test databases that are ephemeral, Snowflake databases persist between test runs. This causes:
- "Previously run changeset" errors
- State contamination between tests
- Test failures due to existing objects

### The Solution: Init-Based Cleanup
We implemented an `init.xml` changeset that:
1. Runs with `runAlways="true"` before each test
2. Cleans DATABASECHANGELOG entries (except init itself)
3. Drops all test objects systematically
4. Ensures a clean state for each test run

## Testing Strategy

### 1. Directory Structure (Following Aurora Pattern)
```
src/main/resources/liquibase/harness/change/
├── changelogs/
│   └── snowflake/
│       ├── init.xml                    # Critical: Cleanup script
│       ├── createTableEnhanced.xml     # Snowflake-specific tests
│       ├── alterSequence.xml
│       └── ...
├── expectedSql/
│   └── snowflake/
│       ├── init.sql                    # Expected cleanup SQL
│       ├── createTableEnhanced.sql
│       └── ...
└── expectedSnapshot/
    └── snowflake/
        ├── init.json                   # Empty snapshot after cleanup
        ├── createTableEnhanced.json
        └── ...
```

### 2. Init Cleanup Implementation
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
         http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">

    <changeSet id="snowflake-cleanup-tracking-tables" author="test-harness" runAlways="true">
        <comment>Clean up Liquibase tracking tables to fix 'previously run' changeset issues</comment>
        
        <!-- Clear all tracking data to ensure clean test state -->
        <sql>DELETE FROM DATABASECHANGELOG WHERE FILENAME NOT LIKE '%init.xml'</sql>
        <sql>UPDATE DATABASECHANGELOGLOCK SET LOCKED = FALSE WHERE ID = 1</sql>
        
        <!-- Clean up any leftover test objects that might interfere -->
        <sql>DROP TABLE IF EXISTS valueSequenceNextTable CASCADE</sql>
        <sql>DROP TABLE IF EXISTS AUTHORS CASCADE</sql>
        <sql>DROP TABLE IF EXISTS BOOKS CASCADE</sql>
        <sql>DROP TABLE IF EXISTS POSTS CASCADE</sql>
        <!-- Add all test objects here -->
        
        <rollback>
            <!-- No rollback needed for cleanup operations -->
        </rollback>
    </changeSet>
</databaseChangeLog>
```

### 3. Test Pattern Requirements

#### Self-Contained Tests
Each test must include setup for any required objects:
```xml
<!-- Example: setTableRemarks test -->
<databaseChangeLog>
    <!-- Create required table first -->
    <changeSet id="createAuthorsTable" author="test-harness">
        <createTable tableName="authors">
            <column name="id" type="int">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="email" type="varchar(100)">
                <constraints nullable="false"/>
            </column>
        </createTable>
    </changeSet>
    
    <!-- Then test the actual change -->
    <changeSet id="1" author="as">
        <setTableRemarks remarks="A Test Remark" tableName="authors"/>
    </changeSet>
</databaseChangeLog>
```

#### Expected SQL Updates
Expected SQL must include all setup operations:
```sql
-- expectedSql/snowflake/setTableRemarks.sql
CREATE TABLE LTHDB.TESTHARNESS.authors (id INT NOT NULL, email VARCHAR(100) NOT NULL, name VARCHAR(100), CONSTRAINT PK_AUTHORS PRIMARY KEY (id))
COMMENT ON TABLE LTHDB.TESTHARNESS.authors IS 'A Test Remark'
```

### 4. Test Execution Pattern
Always run init before each test:
```bash
mvn test -Dtest=ChangeObjectTests -DchangeObjects=init,<testName> -DdbName=snowflake
```

### 5. Common Snowflake-Specific Considerations

#### Data Type Mappings
- `INTEGER` → `INT` in generated SQL
- `TEXT` → `VARCHAR(16777216)` 
- Be aware of Snowflake's automatic type conversions

#### Snowflake Limitations
- No enforced foreign key constraints (unless `UNSUPPORTED_DDL_ACTION = FAIL`)
- No MINVALUE/MAXVALUE/CYCLE for sequences
- DATA_RETENTION_TIME_IN_DAYS minimum is 1 (not 0)

#### Required Dependencies
Ensure `liquibase-snowflake` dependency is uncommented in test harness POM:
```xml
<dependency>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-snowflake</artifactId>
    <version>0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

### 6. Rollback Strategy
The test harness uses `rollbackToDate` which works well with our init pattern:
1. Init runs and cleans state
2. Test changesets execute
3. Snapshot is taken
4. rollbackToDate rolls back to before test changesets
5. Init remains due to `runAlways="true"`

## Implementation Checklist

When adding a new Snowflake test:
1. ✅ Create changelog in `changelogs/snowflake/`
2. ✅ Include any required object setup in the changelog
3. ✅ Create expected SQL in `expectedSql/snowflake/`
4. ✅ Create expected snapshot in `expectedSnapshot/snowflake/`
5. ✅ Add cleanup for any new objects to `init.xml`
6. ✅ Test with: `mvn test -Dtest=ChangeObjectTests -DchangeObjects=init,<testName> -DdbName=snowflake`

## Troubleshooting

### "Previously run changeset" errors
- Ensure init.xml is being run
- Check that DATABASECHANGELOG cleanup is working

### "Table does not exist" errors
- Add table creation to the test's changelog
- Update expected SQL to include creation

### XSD resolution errors
- Verify liquibase-snowflake dependency is uncommented
- Check namespace mappings in extension

### Expected vs Generated SQL mismatches
- Check Snowflake's type conversions (INTEGER→INT)
- Verify expected SQL matches Snowflake's exact output format