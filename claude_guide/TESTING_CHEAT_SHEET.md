# Liquibase Test Harness - Testing Cheat Sheet

## Correct Maven Test Syntax

### Run All Tests for Snowflake
```bash
mvn clean test -DdbName=snowflake
```

### Run Specific Test Class for Snowflake
```bash
mvn test -Dtest=ChangeObjectTests -DdbName=snowflake
```

### Run Specific Change Object Test for Snowflake
```bash
mvn test -Dtest=ChangeObjectTests -DchangeObjects=<changeObjectName> -DdbName=snowflake
```

### Examples of Specific Change Object Tests
```bash
# Test createSequence
mvn test -Dtest=ChangeObjectTests -DchangeObjects=createSequence -DdbName=snowflake

# Test dropWarehouse  
mvn test -Dtest=ChangeObjectTests -DchangeObjects=dropWarehouse -DdbName=snowflake

# Test createDatabase
mvn test -Dtest=ChangeObjectTests -DchangeObjects=createDatabase -DdbName=snowflake
```

## Important Parameters

- `-Dtest=ChangeObjectTests` - The test class (NOT the full package path)
- `-DdbName=snowflake` - Database type (NOT `-Dtest.databases=snowflake`)
- `-DchangeObjects=<name>` - Specific change object to test
- `-DrollbackStrategy=rollbackByTag` - For Snowflake (used in CI)
- `-Dliquibase.secureParsing=false` - Sometimes needed for complex tests

## Test Types Available

1. **ChangeObjectTests** - Tests individual change objects
2. **FoundationalTest** - Basic Liquibase functionality
3. **AdvancedTest** - Advanced features (snapshot, generateChangelog, diff)
4. **GenerateChangelogTest** - Tests generateChangelog command
5. **DiffTest** - Tests diff command

## Snowflake Change Objects Available

### Warehouse Operations
- createWarehouse
- alterWarehouse  
- dropWarehouse

### Database Operations
- createDatabase
- alterDatabase
- dropDatabase

### Schema Operations
- createSchemaEnhanced

### Sequence Operations
- createSequence
- createSequenceEnhanced
- alterSequence
- renameSequence
- dropSequence
- valueSequenceNext

### Function/Procedure Operations
- createFunction
- dropFunction
- createProcedure
- createProcedureFromFile
- dropProcedure

### Package Operations
- createPackage
- createPackageBody

### Table Operations
- createTableEnhanced
- createTableDataTypeDoubleIsFloat
- modifyDataType
- setTableRemarks

### Constraint Operations
- addForeignKey
- dropForeignKey
- dropDefaultValue

### Other
- testSnowflakeSQL

## Test Configuration

Tests expect `harness-config.yml` in `src/test/resources/` with Snowflake connection details.

## Quick Debugging

If a test fails:
1. Check `target/surefire-reports/` for detailed logs
2. Look at expected vs generated SQL in the logs
3. Check if expected SQL file exists in `src/main/resources/liquibase/harness/change/expectedSql/snowflake/`
4. Check if expected snapshot exists in `src/main/resources/liquibase/harness/change/expectedSnapshot/snowflake/`

## Common Issues

- ❌ `-Dtest.databases=snowflake` (WRONG)  
- ✅ `-DdbName=snowflake` (CORRECT)

- ❌ `-Dtest=liquibase.harness.ChangeObjectTests` (WRONG)  
- ✅ `-Dtest=ChangeObjectTests` (CORRECT)