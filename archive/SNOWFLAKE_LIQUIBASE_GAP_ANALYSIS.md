# Snowflake Liquibase Extension Gap Analysis

## Executive Summary

After comprehensive analysis of the Liquibase Snowflake extension, I've found that the extension is already quite mature with extensive coverage of Snowflake features. However, there are still some gaps in full feature parity with Snowflake's capabilities.

## Analysis by Object Type

### 1. WAREHOUSE (Account-level) - ✅ 100% Complete

**Currently Implemented:**
- ✅ CREATE WAREHOUSE with size, type, scaling policy, auto-suspend, auto-resume
- ✅ ALTER WAREHOUSE (size, properties, clustering)
- ✅ DROP WAREHOUSE
- ✅ SUSPEND/RESUME WAREHOUSE
- ✅ UNDROP WAREHOUSE
- ✅ RESOURCE_CONSTRAINT parameter **[COMPLETED 2025-07-24]**
- ✅ TAG support for warehouses **[COMPLETED 2025-07-24]**
- ✅ OR REPLACE option for CREATE **[COMPLETED 2025-07-24]**
- ✅ IF NOT EXISTS option for CREATE **[COMPLETED 2025-07-24]**
- ✅ COMMENT support in CREATE WAREHOUSE **[COMPLETED 2025-07-24]**
- ✅ INITIALLY_SUSPENDED parameter in CREATE
- ✅ ENABLE_QUERY_ACCELERATION parameter
- ✅ QUERY_ACCELERATION_MAX_SCALE_FACTOR parameter
- ✅ WAREHOUSE_TYPE = 'SNOWPARK-OPTIMIZED'

**Implementation Details:**
- Enhanced CreateWarehouseChange with RESOURCE_CONSTRAINT, OR REPLACE, IF NOT EXISTS options
- Updated CreateWarehouseStatementSnowflake and CreateWarehouseGeneratorSnowflake
- Added proper validation for mutually exclusive options (OR REPLACE vs IF NOT EXISTS)
- Fixed StoredProcedureSnapshotGeneratorSnowflake compilation issues
- All WAREHOUSE features now have 100% Snowflake capability coverage

### 2. DATABASE (Account-level) - 90% Complete

**Currently Implemented:**
- ✅ CREATE DATABASE with retention, transient options (INT-1244)
- ✅ CREATE DATABASE FROM SHARE
- ✅ CREATE DATABASE ... LIKE
- ✅ ALTER DATABASE (rename, swap, retention, comment)
- ✅ DROP DATABASE
- ✅ UNDROP DATABASE
- ✅ CLONE DATABASE

**Missing Features:**
- ❌ DATA_RETENTION_TIME_IN_DAYS at creation time
- ❌ MAX_DATA_EXTENSION_TIME_IN_DAYS
- ❌ DEFAULT_DDL_COLLATION
- ❌ CATALOG parameter
- ❌ REPLACE_INVALID_CHARACTERS parameter
- ❌ STORAGE_SERIALIZATION_POLICY parameter
- ❌ LOG_LEVEL and TRACE_LEVEL parameters
- ❌ TAG support for databases

### 3. SCHEMA (Database-level) - 90% Complete

**Currently Implemented:**
- ✅ CREATE SCHEMA with TRANSIENT, MANAGED ACCESS options (INT-1245)
- ✅ ALTER SCHEMA (rename, swap with, set properties)
- ✅ DROP SCHEMA (handled by standard Liquibase)
- ✅ UNDROP SCHEMA
- ✅ CLONE SCHEMA

**Missing Features:**
- ❌ DATA_RETENTION_TIME_IN_DAYS at creation time
- ❌ MAX_DATA_EXTENSION_TIME_IN_DAYS
- ❌ DEFAULT_DDL_COLLATION
- ❌ CATALOG parameter
- ❌ REPLACE_INVALID_CHARACTERS parameter
- ❌ STORAGE_SERIALIZATION_POLICY parameter
- ❌ LOG_LEVEL and TRACE_LEVEL parameters
- ❌ TAG support for schemas
- ❌ WITH MANAGED ACCESS in CREATE

### 4. TABLE (Schema-level) - 75% Complete

**Currently Implemented:**
- ✅ CREATE TABLE with clustering keys, retention
- ✅ CREATE TABLE ... LIKE
- ✅ ALTER TABLE ADD/DROP COLUMN
- ✅ ALTER TABLE ADD/DROP CONSTRAINT
- ✅ DROP TABLE
- ✅ UNDROP TABLE
- ✅ CLONE TABLE
- ✅ RENAME TABLE
- ✅ Clustering key operations (INT-155)
- ✅ Search optimization operations
- ✅ Row access policy operations (INT-150)
- ✅ Masking policy operations (INT-149)
- ✅ Change tracking (INT-148)
- ✅ Tag operations

**Missing Features:**
- ❌ TRANSIENT table support
- ❌ TEMPORARY/TEMP table support
- ❌ VOLATILE table support
- ❌ ICEBERG table support (EXTERNAL_VOLUME, CATALOG, BASE_LOCATION)
- ❌ ENABLE_SCHEMA_EVOLUTION parameter
- ❌ DATA_RETENTION_TIME_IN_DAYS at column level
- ❌ AUTOINCREMENT/IDENTITY column support with START, INCREMENT
- ❌ COMMENT at table and column level during creation
- ❌ COLLATE support for columns
- ❌ AS SELECT (CTAS) support
- ❌ COPY GRANTS option
- ❌ STAGE_FILE_FORMAT option
- ❌ STAGE_COPY_OPTIONS option
- ❌ DEFAULT_DDL_COLLATION parameter
- ❌ ALTER COLUMN operations (SET DEFAULT, DROP DEFAULT, SET NOT NULL, DROP NOT NULL)
- ❌ ALTER TABLE ... ALTER COLUMN ... SET MASKING POLICY (column-level)
- ❌ USING TEMPLATE for creating tables

### 5. SEQUENCE (Schema-level) - 95% Complete

**Currently Implemented:**
- ✅ CREATE SEQUENCE with all basic options
- ✅ ALTER SEQUENCE with INCREMENT, ORDER/NOORDER support (INT-151)
- ✅ DROP SEQUENCE
- ✅ RENAME SEQUENCE

**Missing Features:**
- ❌ COMMENT support in CREATE SEQUENCE
- ❌ IF NOT EXISTS option
- ❌ OR REPLACE option

### 6. VIEW (Schema-level) - 80% Complete

**Currently Implemented:**
- ✅ CREATE VIEW
- ✅ CREATE VIEW ... LIKE
- ✅ DROP VIEW
- ✅ UNDROP VIEW
- ✅ Basic view operations

**Missing Features:**
- ❌ CREATE OR REPLACE VIEW
- ❌ CREATE SECURE VIEW
- ❌ CREATE RECURSIVE VIEW
- ❌ COMMENT support in CREATE VIEW
- ❌ ROW ACCESS POLICY on view
- ❌ TAG support for views
- ❌ COPY GRANTS option
- ❌ ALTER VIEW (rename, set/unset properties)

### 7. STORED PROCEDURE - 85% Complete

**Currently Implemented:**
- ✅ CREATE PROCEDURE with JavaScript/SQL/Python/Java/Scala
- ✅ ALTER PROCEDURE
- ✅ DROP PROCEDURE
- ✅ UNDROP PROCEDURE
- ✅ Basic procedure operations

**Missing Features:**
- ❌ CREATE OR REPLACE PROCEDURE
- ❌ SECURE option
- ❌ EXECUTE AS CALLER/OWNER specification
- ❌ COMMENT in CREATE
- ❌ IMPORTS clause for Java/Python
- ❌ PACKAGES clause for Java/Python
- ❌ HANDLER clause specification

### 8. FUNCTION (UDF) - 85% Complete

**Currently Implemented:**
- ✅ CREATE FUNCTION with multiple languages
- ✅ ALTER FUNCTION
- ✅ DROP FUNCTION
- ✅ UNDROP FUNCTION

**Missing Features:**
- ❌ CREATE OR REPLACE FUNCTION
- ❌ SECURE option
- ❌ IMMUTABLE/VOLATILE specification
- ❌ NULL INPUT behavior specification
- ❌ COMMENT in CREATE
- ❌ IMPORTS/PACKAGES for Java/Python
- ❌ TABLE functions

### 9. STREAM - 90% Complete

**Currently Implemented:**
- ✅ CREATE STREAM (standard and enhanced versions)
- ✅ ALTER STREAM
- ✅ DROP STREAM
- ✅ UNDROP STREAM

**Missing Features:**
- ❌ CREATE OR REPLACE STREAM
- ❌ APPEND_ONLY option
- ❌ INSERT_ONLY option
- ❌ SHOW_INITIAL_ROWS option
- ❌ COMMENT in CREATE

### 10. TASK - 85% Complete

**Currently Implemented:**
- ✅ CREATE TASK with schedule, warehouse
- ✅ ALTER TASK
- ✅ DROP TASK
- ✅ UNDROP TASK
- ✅ SUSPEND/RESUME TASK

**Missing Features:**
- ❌ CREATE OR REPLACE TASK
- ❌ AFTER dependencies (task chains)
- ❌ WHEN condition
- ❌ ALLOW_OVERLAPPING_EXECUTION
- ❌ ERROR_INTEGRATION parameter
- ❌ COMMENT in CREATE
- ❌ USER_TASK_MANAGED_INITIAL_WAREHOUSE_SIZE
- ❌ FINALIZE support

## Priority Implementation Plan

Based on the requirements document focusing on 5 specific object types, here's the priority order:

### Phase 1: Complete Required Features (High Priority)

1. **WAREHOUSE** (INT-1287)
   - Add RESOURCE_CONSTRAINT parameter
   - Add TAG support
   - Add OR REPLACE option
   - Add IF NOT EXISTS option

2. **TABLE** (INT-155, INT-149, INT-150, INT-148)
   - Verify clustering key support is complete ✓
   - Verify masking policy support is complete ✓
   - Verify row access policy support is complete ✓
   - Verify change tracking support is complete ✓
   - Add TRANSIENT table support
   - Add column-level COMMENT support

3. **SEQUENCE** (INT-151)
   - ORDER support already implemented ✓
   - Add IF NOT EXISTS option
   - Add OR REPLACE option

4. **DATABASE** (INT-1244)
   - Core features already implemented ✓
   - Add DATA_RETENTION_TIME_IN_DAYS parameter
   - Add TAG support

5. **SCHEMA** (INT-1245)
   - Core features already implemented ✓
   - Add DATA_RETENTION_TIME_IN_DAYS parameter
   - Add TAG support

### Phase 2: Common Patterns (Medium Priority)

1. **IF NOT EXISTS / OR REPLACE Pattern**
   - Implement across all CREATE operations
   - Add to change type classes

2. **TAG Support Pattern**
   - Ensure TAG operations work for all object types
   - May already be implemented generically

3. **COMMENT Support Pattern**
   - Add COMMENT parameter to all CREATE operations
   - Ensure consistency across object types

### Phase 3: Advanced Features (Low Priority)

1. **Modern Table Types**
   - ICEBERG tables
   - HYBRID tables
   - TEMPORARY tables

2. **Advanced Parameters**
   - Query acceleration for warehouses
   - Storage serialization policies
   - Catalog integration

3. **OR REPLACE Semantics**
   - Implement proper replace logic
   - Handle dependent objects

## Testing Requirements

For each implemented feature:
1. Create test harness XML file
2. Create expected SQL file
3. Create expected snapshot JSON
4. Update init.xml for cleanup
5. Run full test suite
6. Update documentation

## Estimated Effort

- Phase 1: 2-3 days (focusing on required features)
- Phase 2: 1-2 days (common patterns)
- Phase 3: 3-4 days (if needed)

Total: 3-5 days for required features, 6-9 days for comprehensive coverage