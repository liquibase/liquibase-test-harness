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

1. Copy the Snowflake harness config template:
   ```bash
   cp harness-config.snowflake.yml src/test/resources/harness-config.yml
   ```

2. Edit `src/test/resources/harness-config.yml` and replace the placeholders:
   - `<YOUR_ACCOUNT>`: Your Snowflake account identifier
   - `<YOUR_USERNAME>`: Your Snowflake username
   - `<YOUR_PASSWORD>`: Your Snowflake password

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