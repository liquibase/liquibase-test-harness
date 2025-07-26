# AI Extension Developer Guide - Snowflake Test Harness

## CRITICAL: Read This First
This guide is optimized for AI consumption. Follow patterns EXACTLY as shown. Do NOT deviate unless explicitly instructed.

## Recent Updates (2025-07-24)
- ✅ **WAREHOUSE Implementation Complete**: Added RESOURCE_CONSTRAINT, OR REPLACE, IF NOT EXISTS features
- ✅ **Test Organization Standards**: Established best practices for single vs. multiple test files
- ✅ **XSD Schema Issues**: Solutions for attribute validation problems
- ✅ **Build Integration**: Proper rebuild and deployment patterns after code changes

## Decision Tree for New Development

```
START HERE
│
├─ Is it a new Snowflake object type?
│  ├─ YES → Is it schema-level or account-level?
│  │  ├─ Schema-level (TABLE, VIEW, SEQUENCE, FUNCTION, PROCEDURE, STAGE, PIPE, STREAM, TASK)
│  │  │  └─ Use SCHEMA_LEVEL_TEMPLATE
│  │  └─ Account-level (WAREHOUSE, DATABASE, SCHEMA, ROLE, USER, RESOURCE MONITOR)
│  │     └─ Use ACCOUNT_LEVEL_TEMPLATE
│  └─ NO → Is it modifying existing functionality?
│     ├─ YES → Use MODIFICATION_TEMPLATE
│     └─ NO → Use ENHANCEMENT_TEMPLATE
```

## Required Files for EVERY Test

```
ALWAYS create these three files:
1. src/main/resources/liquibase/harness/change/changelogs/snowflake/<testName>.xml
2. src/main/resources/liquibase/harness/change/expectedSql/snowflake/<testName>.sql  
3. src/main/resources/liquibase/harness/change/expectedSnapshot/snowflake/<testName>.json
```

## SCHEMA_LEVEL_TEMPLATE

### Changelog (copy exactly, replace <placeholders>)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns:snowflake="http://www.liquibase.org/xml/ns/snowflake"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd
                      http://www.liquibase.org/xml/ns/snowflake
                      http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd">

    <!-- Independent test - includes cleanup (FOOLPROOF) -->
    <changeSet id="cleanup-test-objects-<testName>" author="test-harness" runAlways="true">
        <comment>Clean up test objects from previous runs</comment>
        <sql>
            DROP SCHEMA IF EXISTS TESTHARNESS CASCADE;
            DELETE FROM DATABASECHANGELOG WHERE FILENAME NOT LIKE '%init.xml';
            UPDATE DATABASECHANGELOGLOCK SET LOCKED = FALSE WHERE ID = 1;
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

    <changeSet id="<testName>-test" author="test-harness">
        <YOUR_CHANGE_TYPE_HERE/>
        <rollback>
            <YOUR_ROLLBACK_HERE/>
        </rollback>
    </changeSet>

</databaseChangeLog>
```

### Expected SQL (copy exactly, add your SQL at end)
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
<YOUR_EXPECTED_SQL_HERE>
```

### Expected Snapshot
```json
{
  "snapshot": {
    "objects": {
      "<OBJECT_TYPE>": [
        {
          "<object_key>": {
            "name": "<OBJECT_NAME>",
            "schema": "TESTHARNESS"
          }
        }
      ]
    }
  }
}
```

## ACCOUNT_LEVEL_TEMPLATE

### Changelog (copy exactly, replace <placeholders>)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns:snowflake="http://www.liquibase.org/xml/ns/snowflake"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd
                      http://www.liquibase.org/xml/ns/snowflake
                      http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd">

    <!-- Independent test - includes cleanup (FOOLPROOF) -->
    <changeSet id="cleanup-test-objects-<testName>" author="test-harness" runAlways="true">
        <comment>Clean up test objects from previous runs</comment>
        <sql>
            DROP <OBJECT_TYPE> IF EXISTS LTHDB_TEST_<NAME1>;
            DROP <OBJECT_TYPE> IF EXISTS LTHDB_TEST_<NAME2>;
            DELETE FROM DATABASECHANGELOG WHERE FILENAME NOT LIKE '%init.xml';
            UPDATE DATABASECHANGELOGLOCK SET LOCKED = FALSE WHERE ID = 1;
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

    <changeSet id="<testName>-test" author="test-harness">
        <snowflake:<changeType> <objectName>="LTHDB_TEST_<NAME>" />
        <rollback>
            <snowflake:<dropType> <objectName>="LTHDB_TEST_<NAME>" />
        </rollback>
    </changeSet>

</databaseChangeLog>
```

## Validation Checklist (MUST pass all)

Before running ANY test:
- [ ] First changeset is cleanup with runAlways="true" (FOOLPROOF)
- [ ] NO SQL comments in cleanup changeset (no -- comments)  
- [ ] Cleanup includes DELETE/UPDATE DATABASECHANGELOG* statements
- [ ] Expected SQL includes full cleanup SQL at beginning
- [ ] Account-level objects use LTHDB_TEST_ prefix
- [ ] Account-level objects dropped BEFORE schema in cleanup
- [ ] All three files created (changelog, expectedSql, expectedSnapshot)

## Test Organization Standards (CRITICAL)

### When to Create Separate Test Files vs. Combined Tests

**SEPARATE TEST FILES (Recommended Default):**
- ✅ **Different Core Functionality** - Each major feature gets its own file
  - `createWarehouse.xml`, `alterWarehouse.xml`, `dropWarehouse.xml`
  - `createWarehouseWithResourceConstraint.xml`, `createWarehouseIfNotExists.xml`
- ✅ **Mutually Exclusive Features** - Features that conflict with each other
  - `createOrReplaceWarehouse.xml` vs `createWarehouseIfNotExists.xml` (OR REPLACE vs IF NOT EXISTS)
- ✅ **Independent Validation** - When testing features in isolation
- ✅ **Different Rollback Behavior** - When rollback scenarios differ significantly

**COMBINED TESTS (Use Sparingly):**
- ✅ **Multiple Changesets in Same File** - For variations of the same operation
  - `createWarehouse.xml` has 3 changesets: basic, multicluster, advanced
- ✅ **Sequential Workflows** - When testing operational sequences
  - Create → Alter → Drop in logical progression
- ✅ **Feature Compatibility** - Ensuring non-conflicting features work together

### Established Pattern from Existing Codebase:
```
✅ CORRECT: 1 test file per major feature/operation
✅ CORRECT: Multiple changesets within file for variations  
✅ CORRECT: Separate files for conflicting options
✅ CORRECT: Each test is self-contained with proper cleanup
```

### Test File Naming Convention:
```
- createWarehouse.xml                    # Basic operation
- createWarehouseWithResourceConstraint.xml  # New parameter feature
- createOrReplaceWarehouse.xml          # Different creation behavior
- createWarehouseIfNotExists.xml        # Mutually exclusive option
```

## Test Execution Pattern

```bash
# SIMPLE - Tests are now independent and foolproof
export LIQUIBASE_LICENSE_KEY="<key>"
mvn clean test -Dtest=ChangeObjectTests -DchangeObjects=<testName> -DdbName=snowflake

# No need to call init separately - each test includes its own cleanup changeset!
```

## Error Resolution Matrix

| Error | Solution | Template Section |
|-------|----------|-----------------|
| "Expected sql doesn't match" | Remove SQL comments from init | VALIDATION_CHECKLIST |
| "CREATE TABLE missing" | Include init SQL in expected | EXPECTED_SQL |
| "Object already exists" | Add DROP in init changeset | ACCOUNT_LEVEL_TEMPLATE |
| "Pro license required" | Set LIQUIBASE_LICENSE_KEY | TEST_EXECUTION |
| "Snapshot mismatch" | Update expected snapshot | EXPECTED_SNAPSHOT |

## Working Examples to Copy

### Schema-Level: createTable
- Changelog: `changelogs/snowflake/createTableEnhanced.xml`
- Expected SQL: `expectedSql/snowflake/createTableEnhanced.sql`
- Snapshot: `expectedSnapshot/snowflake/createTableEnhanced.json`

### Account-Level: createWarehouse  
- Changelog: `changelogs/snowflake/createWarehouse.xml`
- Expected SQL: `expectedSql/snowflake/createWarehouse.sql`
- Snapshot: `expectedSnapshot/snowflake/createWarehouse.json`

## Implementation Workflow (COMPLETE PROCESS)

### Phase 1: Code Implementation
```bash
# 1. Implement Change class, Statement class, SQL Generator class in liquibase-snowflake
# 2. CRITICAL: Rebuild and deploy the extension JAR after ANY code changes
mvn package -DskipTests -f /path/to/liquibase-snowflake/pom.xml
cp target/liquibase-snowflake-1.0.0-SNAPSHOT.jar ai-generated/liquibase-4.33.0/lib/

# 3. Fix compilation errors using established patterns:
#    - Use JdbcConnection instead of getWrappedConnection()
#    - Import liquibase.database.jvm.JdbcConnection
#    - Follow existing snapshot generator patterns
```

### Phase 2: Test Harness Creation
1. **IDENTIFY**: Schema-level or account-level?
2. **COPY**: Use exact template for type
3. **MODIFY**: Replace only <placeholders>
4. **CREATE**: All three required files (changelog, expectedSql, expectedSnapshot)
5. **UPDATE**: init.xml with cleanup for new objects
6. **VALIDATE**: Run through checklist
7. **TEST**: Use exact test command with init first
8. **DEBUG**: Use error matrix if needed

### Phase 3: XSD Schema Resolution (if needed)
**Problem**: `cvc-complex-type.3.2.2: Attribute 'X' is not allowed`
**Solution**: 
- Update liquibase-snowflake-latest.xsd with new attributes
- Ensure extension JAR is in classpath 
- Rebuild extension after XSD changes

### Phase 4: Implementation Patterns (Copy These Exactly)

**JdbcConnection Pattern**:
```java
JdbcConnection connection = (JdbcConnection) database.getConnection();
try (Statement statement = connection.createStatement()) {
    // SQL execution here
}
```

**Validation Pattern**:
```java
if (statement.getOrReplace() != null && statement.getOrReplace() && 
    statement.getIfNotExists() != null && statement.getIfNotExists()) {
    errors.addError("OR REPLACE and IF NOT EXISTS cannot be used together");
}
```

**SQL Generation Pattern**:
```java
if (statement.getOrReplace() != null && statement.getOrReplace()) {
    sql.append("OR REPLACE ");
}
```

## DO NOT

- Add SQL comments to init changesets
- Forget runAlways="true"
- Skip any of the three required files
- Use custom naming for account objects
- Change the init SQL structure
- Omit init SQL from expected files
- **Skip rebuilding extension JAR after code changes**
- **Test without running init first**
- **Use getWrappedConnection() pattern (use JdbcConnection)**
- **Create multiple test files for variations of same feature (use multiple changesets)**
- **Assume XSD schema has new attributes (may need manual updates)**

## ALWAYS

- Copy templates exactly  
- Include full cleanup SQL in expected (at beginning)
- Use LTHDB_TEST_ for account objects
- Drop account objects before schema in cleanup
- Run mvn clean before testing
- Set Pro license key for Pro features
- **Rebuild and redeploy extension JAR after ANY code changes**
- **Include cleanup changeset as first changeset (FOOLPROOF)**
- **Follow established test organization standards**
- **Use JdbcConnection pattern for database connections**
- **Follow separate test files for mutually exclusive features**
- **Each test must be completely independent and self-contained**

## SUCCESS CASE STUDY: WAREHOUSE Implementation (2025-07-24)

### What Was Implemented
Added missing WAREHOUSE features: RESOURCE_CONSTRAINT, OR REPLACE, IF NOT EXISTS

### Code Changes Made
1. **CreateWarehouseChange.java**: Added new properties with getters/setters
2. **CreateWarehouseStatementSnowflake.java**: Added matching properties  
3. **CreateWarehouseGeneratorSnowflake.java**: Enhanced SQL generation and validation
4. **StoredProcedureSnapshotGeneratorSnowflake.java**: Fixed compilation using JdbcConnection pattern

### Test Harness Files Created
Following the separate test file standard:
- `createWarehouseWithResourceConstraint.xml` - New parameter feature
- `createOrReplaceWarehouse.xml` - Different creation behavior (with setup changeset)
- `createWarehouseIfNotExists.xml` - Mutually exclusive option (with duplicate test)

### Key Learnings Applied
- ✅ **Separate files for mutually exclusive features** (OR REPLACE vs IF NOT EXISTS)
- ✅ **Multiple changesets for feature variations** (initial create, then test behavior)
- ✅ **Self-contained tests** (each test creates its own objects)
- ✅ **Proper cleanup** (added new warehouse names to init.xml)
- ✅ **Build integration** (rebuilt extension JAR after code changes)

### Results
- 100% WAREHOUSE feature coverage achieved
- All tests follow established patterns
- Clean separation of concerns
- Ready for next object type implementation

**Use this as the template for future implementations.**