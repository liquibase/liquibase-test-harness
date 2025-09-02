# Snowflake Test Harness Lifecycle Scripts

This directory contains Snowflake-specific lifecycle scripts for the Liquibase Test Harness.

## Scripts

### suite-init.sql
- **Purpose**: One-time setup of the Snowflake test environment
- **When it runs**: Should be run manually before running the test suite for the first time
- **Requirements**: Must be run with ACCOUNTADMIN privileges or equivalent
- **What it does**:
  - Creates the LTHDB database
  - Creates the TESTHARNESS schema
  - Creates the LIQUIBASE_TEST_HARNESS_ROLE role
  - Sets up all necessary permissions
  - Creates test warehouses (LTHDB_TEST_WH, XSMALL_WH)
  - Creates Liquibase tracking tables

**Important**: Update the username from 'COMMUNITYKEVIN' to your actual Snowflake username before running.

### init.sql
- **Purpose**: Pre-test cleanup to ensure clean state
- **When it runs**: Before each test (when lifecycle hooks are enabled)
- **What it does**:
  - Unlocks DATABASECHANGELOGLOCK
  - Cleans DATABASECHANGELOG entries (except init.xml)

### cleanup.sql
- **Purpose**: Post-test cleanup
- **When it runs**: After each test (when lifecycle hooks are enabled)
- **What it does**:
  - Cleans DATABASECHANGELOG entries (except init.xml)
  - Ensures DATABASECHANGELOGLOCK is released

## Usage

### One-Time Setup
Run the suite-init.sql script in Snowflake before running tests:
```sql
-- Run as ACCOUNTADMIN
!source suite-init.sql
```

### Per-Test Cleanup
Enable lifecycle hooks when running tests:
```bash
mvn test -DdbName=snowflake -Dliquibase.harness.lifecycle.enabled=true
```

## Notes
- The suite-init.sql script is NOT automatically executed by the test harness
- It must be run manually as part of initial Snowflake setup
- The init.sql and cleanup.sql scripts ARE automatically executed when lifecycle hooks are enabled