# Cloud Database Initialization

## Overview

The Liquibase Test Harness now includes automatic initialization support for cloud databases. This feature addresses the need for one-time setup of persistent cloud databases (AWS RDS, GCP Cloud SQL, Azure Database, Snowflake, etc.) that cannot be destroyed and recreated like Docker containers.

## Features

- **Automatic Detection**: Recognizes cloud databases by URL patterns
- **One-time Execution**: Runs once per database configuration, even across test restarts
- **Database-specific Scripts**: Different initialization for different database types
- **Flexible Format**: Supports both SQL scripts and Liquibase changelogs
- **Graceful Error Handling**: Configurable behavior on initialization failures

## Configuration

### Basic Setup

In your `harness-config.yml`, add initialization configuration to your cloud databases:

```yaml
testDatabases:
  # Snowflake Example
  - name: snowflake
    version: latest
    url: jdbc:snowflake://myaccount.snowflakecomputing.com/?db=LTHDB&schema=TESTHARNESS
    username: ${SNOWFLAKE_USERNAME}
    password: ${SNOWFLAKE_PASSWORD}
    initScript: harness/init/snowflake/cloud-init.sql
    
  # AWS RDS MySQL Example
  - name: mysql
    version: 8.0
    url: jdbc:mysql://mydb.cluster-abc123.us-east-1.rds.amazonaws.com:3306/lbtest
    username: ${AWS_DB_USER}
    password: ${AWS_DB_PASS}
    initScript: harness/init/mysql/cloud-init.sql
    
  # GCP Cloud SQL PostgreSQL Example  
  - name: postgresql
    version: 13
    url: jdbc:postgresql://10.20.30.40:5432/lbtest?cloudSqlInstance=project:region:instance
    username: ${GCP_DB_USER}
    password: ${GCP_DB_PASS}
    initChangelog: harness/init/postgresql/cloud-init-changelog.xml
```

### Configuration Options

- `initScript`: Path to SQL script for initialization
- `initChangelog`: Path to Liquibase changelog for initialization
- `skipInit`: Set to `true` to skip initialization for this database
- `initProperties`: Additional connection properties (optional)

## Directory Structure

Place initialization scripts in:

```
src/test/resources/
└── harness/
    └── init/
        ├── snowflake/
        │   └── cloud-init.sql
        ├── mysql/
        │   ├── cloud-init.sql
        │   └── cloud-init-changelog.xml
        ├── postgresql/
        │   └── cloud-init.sql
        └── mssql/
            └── cloud-init.sql
```

## Snowflake Initialization Script

The provided Snowflake initialization script (`harness/init/snowflake/cloud-init.sql`) sets up:

1. **Database and Schema**:
   - Creates `LTHDB` database
   - Creates `TESTHARNESS` schema with 1-day retention

2. **Role and Permissions**:
   - Creates `LIQUIBASE_TEST_HARNESS_ROLE` role
   - Grants role to current user automatically
   - Sets up all necessary object and future object privileges

3. **Warehouses**:
   - Creates `LTHDB_TEST_WH` (primary test warehouse)
   - Creates `XSMALL_WH` (secondary warehouse for testing)

4. **Liquibase Tables**:
   - Creates `DATABASECHANGELOG` table
   - Creates `DATABASECHANGELOGLOCK` table
   - Initializes lock table and cleans stale entries

## Cloud Provider Detection

The following URL patterns are automatically detected as cloud databases:

- AWS RDS: `*.rds.amazonaws.com`
- AWS Aurora: `cluster-*.amazonaws.com`
- Azure Database: `*.database.windows.net`, `*.database.azure.com`
- GCP Cloud SQL: `*.googleapis.com`
- Oracle Cloud: `*.oraclecloud.com`
- Snowflake: `*.snowflakecomputing.com`
- Aiven: `*.db.aiven.io`

## System Properties

Control initialization behavior with these properties:

```bash
# Fail tests if initialization fails (default: false)
-Dliquibase.harness.cloud.init.failOnError=true

# Enable debug logging (default: false)
-Dliquibase.harness.cloud.init.debug=true

# Continue on SQL errors (default: true)
-Dliquibase.harness.cloud.init.continueOnSqlError=false
```

## Usage Examples

### Running Tests with Cloud Database Init

```bash
# Basic usage - initialization happens automatically
mvn test -DdbName=snowflake

# With debug logging
mvn test -DdbName=snowflake -Dliquibase.harness.cloud.init.debug=true

# Fail on initialization errors
mvn test -DdbName=snowflake -Dliquibase.harness.cloud.init.failOnError=true
```

### Skip Initialization for Local Testing

```yaml
testDatabases:
  - name: snowflake
    version: latest
    url: jdbc:snowflake://localhost:8080/test
    username: test
    password: test
    skipInit: true  # Skip for local mock
```

## Writing Initialization Scripts

### Best Practices

1. **Make scripts idempotent** - Use `IF NOT EXISTS` clauses
2. **Handle existing objects** - Don't fail if objects already exist
3. **Use current user** - For Snowflake, use `IDENTIFIER($CURRENT_USER())`
4. **Clean up first** - Remove stale test data before creating new
5. **Set appropriate timeouts** - Cloud databases may have longer latencies

### Example: Snowflake Init Script

```sql
-- Create database if not exists
CREATE DATABASE IF NOT EXISTS LTHDB;
USE DATABASE LTHDB;

-- Create schema with retention policy
CREATE SCHEMA IF NOT EXISTS TESTHARNESS
    DATA_RETENTION_TIME_IN_DAYS = 1;

-- Create role and grant to current user
CREATE ROLE IF NOT EXISTS LIQUIBASE_TEST_HARNESS_ROLE;
GRANT ROLE LIQUIBASE_TEST_HARNESS_ROLE TO USER IDENTIFIER($CURRENT_USER());

-- Set up permissions
GRANT ALL PRIVILEGES ON SCHEMA TESTHARNESS TO ROLE LIQUIBASE_TEST_HARNESS_ROLE;
```

## Troubleshooting

### Common Issues

1. **Permission Denied**
   - Ensure the database user has sufficient privileges
   - For Snowflake, may need ACCOUNTADMIN for initial setup

2. **Script Not Found**
   - Check the script path in configuration
   - Ensure script is in `src/test/resources/harness/init/`

3. **Already Initialized**
   - The system tracks initialized databases
   - To force re-initialization, restart the test JVM

### Debug Mode

Enable debug logging to see:
- Which databases are being initialized
- Script execution details
- SQL statements being run
- Error details

```bash
mvn test -Dliquibase.harness.cloud.init.debug=true
```

## Integration with Lifecycle Hooks

Cloud database initialization works alongside per-test lifecycle hooks:

1. **Cloud Init** (this feature): One-time setup when test suite starts
2. **Lifecycle Hooks**: Per-test cleanup before/after each test

Both can be used together for complete test isolation.

## Future Enhancements

- Support for initialization scripts from external URLs (S3, etc.)
- Retry logic for transient cloud errors
- Initialization verification checks
- Performance metrics collection
- Support for managed database services (Amazon Aurora Serverless, etc.)