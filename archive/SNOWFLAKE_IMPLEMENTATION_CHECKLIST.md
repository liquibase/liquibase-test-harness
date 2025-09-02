# Snowflake Implementation Checklist

## Overview
This checklist tracks the implementation of missing Snowflake features in the Liquibase extension based on the requirements document.

## Required Features by Priority

### 1. WAREHOUSE Operations (INT-1287)

#### CreateWarehouseChange Enhancements
- [ ] Add RESOURCE_CONSTRAINT parameter
  - [ ] Update CreateWarehouseChange class
  - [ ] Update CreateWarehouseStatement class
  - [ ] Update CreateWarehouseGeneratorSnowflake class
  - [ ] Add test: createWarehouseWithResourceConstraint.xml
  
- [ ] Add TAG support
  - [ ] Add tag parameter to CreateWarehouseChange
  - [ ] Update SQL generation
  - [ ] Add test: createWarehouseWithTag.xml
  
- [ ] Add OR REPLACE option
  - [ ] Add orReplace boolean parameter
  - [ ] Update SQL generation
  - [ ] Add test: createOrReplaceWarehouse.xml
  
- [ ] Add IF NOT EXISTS option
  - [ ] Add ifNotExists boolean parameter
  - [ ] Update SQL generation
  - [ ] Add test: createWarehouseIfNotExists.xml

- [ ] Add missing standard parameters
  - [ ] COMMENT parameter
  - [ ] INITIALLY_SUSPENDED parameter
  - [ ] ENABLE_QUERY_ACCELERATION parameter
  - [ ] QUERY_ACCELERATION_MAX_SCALE_FACTOR parameter
  - [ ] WAREHOUSE_TYPE = 'SNOWPARK-OPTIMIZED'

### 2. TABLE Operations (INT-155, INT-149, INT-150, INT-148)

#### Verify Existing Features
- [x] Clustering key support (AddClusteringKeyChange) - COMPLETE
- [x] Masking policy support (SetMaskingPolicyChange) - COMPLETE
- [x] Row access policy support (ApplyRowAccessPolicyChange) - COMPLETE
- [x] Change tracking support (SetChangeTrackingChange) - COMPLETE

#### CreateTableChangeSnowflake Enhancements
- [ ] Add TRANSIENT table support
  - [ ] Add transient boolean parameter
  - [ ] Update SQL generation
  - [ ] Add test: createTransientTable.xml
  
- [ ] Add TEMPORARY table support
  - [ ] Add temporary boolean parameter
  - [ ] Update SQL generation
  - [ ] Add test: createTemporaryTable.xml
  
- [ ] Add column-level COMMENT support
  - [ ] Extend column definition to include comments
  - [ ] Update SQL generation
  - [ ] Add test: createTableWithColumnComments.xml

- [ ] Add AUTOINCREMENT/IDENTITY support
  - [ ] Add autoIncrement parameters (start, increment)
  - [ ] Update SQL generation
  - [ ] Add test: createTableWithIdentity.xml

### 3. SEQUENCE Operations (INT-151)

#### Verify Existing Features
- [x] ORDER/NOORDER support in AlterSequenceChangeSnowflake - COMPLETE

#### CreateSequenceChangeSnowflake Enhancements
- [ ] Add IF NOT EXISTS option
  - [ ] Add ifNotExists boolean parameter
  - [ ] Update SQL generation
  - [ ] Add test: createSequenceIfNotExists.xml
  
- [ ] Add OR REPLACE option
  - [ ] Add orReplace boolean parameter
  - [ ] Update SQL generation
  - [ ] Add test: createOrReplaceSequence.xml
  
- [ ] Add COMMENT support
  - [ ] Add comment parameter
  - [ ] Update SQL generation
  - [ ] Add test: createSequenceWithComment.xml

### 4. DATABASE Operations (INT-1244)

#### Verify Existing Features
- [x] Basic CREATE DATABASE with options - COMPLETE
- [x] ALTER DATABASE operations - COMPLETE

#### CreateDatabaseChange Enhancements
- [ ] Add DATA_RETENTION_TIME_IN_DAYS parameter
  - [ ] Add to CreateDatabaseChange
  - [ ] Update SQL generation
  - [ ] Add test: createDatabaseWithRetention.xml
  
- [ ] Add TAG support
  - [ ] Add tag parameter
  - [ ] Update SQL generation
  - [ ] Add test: createDatabaseWithTag.xml

- [ ] Add additional parameters
  - [ ] MAX_DATA_EXTENSION_TIME_IN_DAYS
  - [ ] DEFAULT_DDL_COLLATION
  - [ ] CATALOG parameter
  - [ ] STORAGE_SERIALIZATION_POLICY

### 5. SCHEMA Operations (INT-1245)

#### Verify Existing Features
- [x] Basic CREATE SCHEMA with TRANSIENT, MANAGED ACCESS - COMPLETE
- [x] ALTER SCHEMA operations - COMPLETE

#### CreateSchemaChangeSnowflake Enhancements
- [ ] Add DATA_RETENTION_TIME_IN_DAYS parameter
  - [ ] Add to CreateSchemaChangeSnowflake
  - [ ] Update SQL generation
  - [ ] Add test: createSchemaWithRetention.xml
  
- [ ] Add TAG support
  - [ ] Add tag parameter
  - [ ] Update SQL generation
  - [ ] Add test: createSchemaWithTag.xml

- [ ] Fix WITH MANAGED ACCESS syntax
  - [ ] Verify current implementation
  - [ ] Update if needed
  - [ ] Add test: createManagedAccessSchema.xml

## Implementation Pattern

For each feature:

1. **Update Change Class**
   ```java
   private String resourceConstraint;
   
   public String getResourceConstraint() {
       return resourceConstraint;
   }
   
   public void setResourceConstraint(String resourceConstraint) {
       this.resourceConstraint = resourceConstraint;
   }
   ```

2. **Update Statement Class**
   ```java
   private String resourceConstraint;
   // Add getter/setter
   ```

3. **Update SQL Generator**
   ```java
   if (statement.getResourceConstraint() != null) {
       sql.append(" RESOURCE_CONSTRAINT = ").append(statement.getResourceConstraint());
   }
   ```

4. **Create Test Harness Files**
   - changelogs/snowflake/createWarehouseWithResourceConstraint.xml
   - expectedSql/snowflake/createWarehouseWithResourceConstraint.sql
   - expectedSnapshot/snowflake/createWarehouseWithResourceConstraint.json

5. **Update Documentation**
   - Update change type documentation
   - Add examples
   - Update test status

## Testing Strategy

1. **Unit Tests**
   - Test each parameter individually
   - Test parameter combinations
   - Test null/empty handling

2. **Integration Tests**
   - Run against real Snowflake instance
   - Verify object creation
   - Test rollback scenarios

3. **Test Harness**
   - Create comprehensive test files
   - Verify SQL generation
   - Validate snapshots

## Success Criteria

- [ ] All required parameters implemented
- [ ] All tests passing
- [ ] Documentation updated
- [ ] Code reviewed and merged
- [ ] Test harness updated

## Notes

- TAG support might already be generically implemented via SetTagChange/UnsetTagChange
- Some features may require Snowflake account-level permissions to test
- Consider backwards compatibility when adding new parameters
- Use existing patterns from other change types as reference