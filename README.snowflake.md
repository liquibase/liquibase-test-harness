# Running Liquibase Test Harness for Snowflake

## Prerequisites

1. Set up the Snowflake test environment using the SQL script:
   ```sql
   -- Run setup-snowflake-test-environment.sql
   ```

2. Build and install the Snowflake extension:
   ```bash
   cd /path/to/liquibase-snowflake
   mvn clean install
   ```

## Configuration

1. Create the harness config file:
   ```bash
   cp src/test/resources/harness-config.local.yml src/test/resources/harness-config.yml
   ```

2. Edit `src/test/resources/harness-config.yml` with the CORRECT database/schema for test harness:
   ```yaml
   databasesUnderTest:
     - name: snowflake
       prefix: local
       version: latest
       url: "jdbc:snowflake://<YOUR_ACCOUNT>.snowflakecomputing.com/?db=LTHDB&warehouse=LTHDB_TEST_WH&schema=TESTHARNESS&role=LIQUIBASE_TEST_HARNESS_ROLE"
       username: "<YOUR_USERNAME>"
       password: "<YOUR_PASSWORD>"
   ```

   **CRITICAL**: Test harness REQUIRES these exact values:
   - Database: `LTHDB` (NOT LIQUIBASE_SNOWFLAKE_TEST)
   - Schema: `TESTHARNESS` (NOT TEST_SCHEMA)
   - Role: `LIQUIBASE_TEST_HARNESS_ROLE`
   - Warehouse: `LTHDB_TEST_WH`

## Running Tests

To run all Snowflake test harness tests:

```bash
mvn clean test -DdbName=snowflake
```

To run a specific test class:

```bash
mvn clean test -Dtest=ChangeObjectTests -DdbName=snowflake
```

## Test Coverage

The test harness includes tests for all Snowflake-specific features:

- **Warehouse Operations**: createWarehouse, alterWarehouse, dropWarehouse
- **Database Operations**: createDatabase, alterDatabase, dropDatabase
- **Schema Operations**: createSchemaEnhanced
- **Sequence Operations**: createSequence, createSequenceEnhanced, alterSequence, renameSequence, dropSequence
- **Table Operations**: createTable, createTableEnhanced, dropTable, renameTable
- **Standard Liquibase Operations**: All standard operations supported by Snowflake

## Namespace Convention

All test resources use the `LTHDB_` prefix to avoid conflicts:
- Warehouses: `LTHDB_TEST_WAREHOUSE`, `LTHDB_ANALYTICS_WAREHOUSE`
- Databases: `LTHDB_TEST_DATABASE`, `LTHDB_STAGING_DB`
- Database/Schema: `LTHDB.TESTHARNESS`

## Troubleshooting

1. **Cannot find harness-config.yml in classpath**: Ensure the config file is in `src/test/resources/`
2. **Database snowflake latest is offline**: Check your Snowflake credentials and connection URL
3. **Permission errors**: Ensure the LIQUIBASE_TEST_HARNESS_ROLE has all necessary privileges
4. **"CREATE TABLE LTHDB.null.DATABASECHANGELOGLOCK" error**: This indicates wrong database/schema in config. Test harness MUST use LTHDB/TESTHARNESS, not integration test values
5. **Test harness vs Integration tests confusion**: 
   - Integration tests use: LIQUIBASE_SNOWFLAKE_TEST/TEST_SCHEMA
   - Test harness uses: LTHDB/TESTHARNESS
   - These are DIFFERENT environments - don't mix them!