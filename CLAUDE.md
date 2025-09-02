# Claude AI - Liquibase Test Harness Knowledge Base

## CRITICAL: NO WORKAROUNDS POLICY

**NEVER implement workarounds without explicit user approval.** When encountering obstacles:
1. **STOP** - Do not proceed with alternative solutions
2. **EXPLAIN** - Clearly describe the problem encountered
3. **ASK** - Request user guidance on how to properly solve the issue
4. **WAIT** - Do not continue until receiving explicit direction

The goal is to implement proper solutions, not quick fixes. Namespace-prefixed attributes like `snowflake:transient="true"` MUST work as designed. Do NOT use tablespace or remarks field encoding as workarounds.

## Recent Updates (2025-07-24)
- ✅ **WAREHOUSE Implementation Complete**: Successfully added RESOURCE_CONSTRAINT, OR REPLACE, IF NOT EXISTS features
- ✅ **Test Organization Standards**: Established best practices for separate vs. combined test files
- ✅ **Build Integration Workflow**: Critical patterns for rebuilding extension JAR after code changes
- ✅ **XSD Schema Resolution**: Solutions for attribute validation issues
- ✅ **JdbcConnection Pattern**: Fixed compilation errors in snapshot generators

## Critical Learnings from Snowflake Test Harness Implementation

### 1. The Persistent Database State Problem
**Key Discovery**: Unlike Docker-based test databases, Snowflake (and other cloud databases) persist between test runs, causing DATABASECHANGELOG to retain execution history.

**Symptoms**:
- "Database is up to date, no changesets to execute"
- "Previously run: 1" errors
- Tests fail because changesets won't re-execute

**Solution**: Implement an init.xml cleanup script with `runAlways="true"` that:
```xml
<sql>DELETE FROM DATABASECHANGELOG WHERE FILENAME NOT LIKE '%init.xml'</sql>
<sql>UPDATE DATABASECHANGELOGLOCK SET LOCKED = FALSE WHERE ID = 1</sql>
```

### 2. Test Harness File Structure Requirements
Every test needs THREE files:
- `changelogs/snowflake/<testName>.xml` - The actual changeset
- `expectedSql/snowflake/<testName>.sql` - Expected generated SQL
- `expectedSnapshot/snowflake/<testName>.json` - Expected database state

Missing any of these causes cryptic test failures.

### 3. Self-Contained Test Pattern
**Critical**: Each test must be self-contained with its own setup. Example:
```xml
<!-- Don't assume tables exist - create them in the test -->
<changeSet id="setup" author="test-harness">
    <createTable tableName="authors">...</createTable>
</changeSet>
<changeSet id="actual-test" author="test">
    <setTableRemarks tableName="authors" remarks="Test"/>
</changeSet>
```

### 4. Expected SQL Must Include Setup
The expectedSql file must include ALL SQL, including setup:
```sql
CREATE TABLE LTHDB.TESTHARNESS.authors (...)
COMMENT ON TABLE LTHDB.TESTHARNESS.authors IS 'Test Remark'
```

### 5. Snowflake-Specific Type Conversions
- Liquibase XML: `type="int"` → Snowflake SQL: `INT` (not INTEGER)
- Be precise in expected SQL files

### 6. XSD Resolution for Extensions
Extensions require their JAR on classpath. In test harness POM:
```xml
<dependency>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-snowflake</artifactId>
    <version>0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

### 7. Test Execution Pattern
Always run init first:
```bash
mvn test -Dtest=ChangeObjectTests -DchangeObjects=init,<testName> -DdbName=snowflake
```

### 8. Cleanup Maintenance
When adding new tests, ALWAYS update init.xml to drop any new objects:
```xml
<sql>DROP TABLE IF EXISTS <new_table_name> CASCADE</sql>
```

### 9. Schema Isolation Implementation (2025-07-30)
**Key Discovery**: Cloud databases need schema isolation to prevent test interference.

**Solution**: Implemented `SchemaIsolationHook` that:
- Creates unique `TEST_<TESTNAME>` schemas for each test
- Configurable per database via `useSchemaIsolation: true`
- Automatically cleans up after test completion
- Updates expected SQL to use isolated schema names

**Implementation Pattern**:
```yaml
lifecycleHooks:
  enabled: true
databasesUnderTest:
  - name: snowflake
    useSchemaIsolation: true
```

**Critical Learnings**:
- Must switch to original schema before dropping test schema
- Expected SQL files must be updated to use TEST_<TESTNAME> schema
- Test-level init scripts supported via `{testName}.init.sql`
- Hanging issues resolved by proper schema context switching

## Common Pitfalls to Avoid

1. **Don't assume ephemeral databases** - Cloud databases persist
2. **Don't skip init** - It's critical for clean state
3. **Don't forget expected files** - All three are required
4. **Don't assume objects exist** - Create them in your test
5. **Don't ignore type conversions** - Match Snowflake's exact output

## Testing Philosophy

The test harness was designed for ephemeral Docker databases. For persistent cloud databases:
1. Embrace the init cleanup pattern
2. Make tests completely self-contained
3. Always clean up after yourself
4. Test the full lifecycle (create → test → rollback)

## Additional Documentation

See the `claude_guide/` directory for:
- Detailed testing strategies
- Test harness patterns
- Maven command cheat sheet

## Implementation and Testing Workflow (2025-07-24 Standards)

### Complete Implementation Process
1. **Code Implementation Phase**:
   ```bash
   # Implement Change, Statement, SQL Generator classes
   mvn package -DskipTests -f /path/to/liquibase-snowflake/pom.xml
   cp target/liquibase-snowflake-1.0.0-SNAPSHOT.jar ai-generated/liquibase-4.33.0/lib/
   ```

2. **Test Organization Standards**:
   - ✅ **Separate test files** for different core functionality
   - ✅ **Separate test files** for mutually exclusive features (OR REPLACE vs IF NOT EXISTS)
   - ✅ **Multiple changesets** within same file for variations of same operation
   - ✅ **Self-contained tests** with proper setup and cleanup

3. **Test Harness Creation**:
   - Create `changelogs/snowflake/testName.xml` - Follow naming: `createWarehouseWithResourceConstraint.xml`
   - Create `expectedSql/snowflake/testName.sql` - Include ALL SQL including init
   - Create `expectedSnapshot/snowflake/testName.json` - Expected database state
   - Update `init.xml` to drop any new objects

4. **Critical Build Integration**:
   - ALWAYS rebuild extension JAR after ANY code changes
   - Tests are now independent: `mvn test -Dtest=ChangeObjectTests -DchangeObjects=testName -DdbName=snowflake`
   - Each test includes its own cleanup changeset (FOOLPROOF)

### Test Organization Examples (From WAREHOUSE Implementation)
```
✅ CORRECT:
- createWarehouse.xml (basic operation with 3 changesets for variations)
- createWarehouseWithResourceConstraint.xml (new parameter feature)
- createOrReplaceWarehouse.xml (different creation behavior)
- createWarehouseIfNotExists.xml (mutually exclusive with OR REPLACE)

❌ INCORRECT:
- createWarehouseAllFeatures.xml (mixing mutually exclusive features)
- One file with OR REPLACE and IF NOT EXISTS in same test
```

## Quick Reference

### Adding a New Test (FOOLPROOF Process)
1. Create `changelogs/snowflake/myTest.xml` with cleanup changeset + test
2. Create `expectedSql/snowflake/myTest.sql` with ALL SQL (including cleanup)
3. Create `expectedSnapshot/snowflake/myTest.json` with expected state
4. Rebuild extension if code changes: `mvn package -DskipTests && cp target/*.jar lib/`
5. Run: `mvn test -Dtest=ChangeObjectTests -DchangeObjects=myTest -DdbName=snowflake`

**No need to call init or update init.xml - each test is completely independent!**

### Debugging Failed Tests
1. Check if init is running (look for cleanup SQL in logs)
2. Verify all three expected files exist
3. Compare generated vs expected SQL character by character
4. Check for "previously run" errors indicating dirty state
5. Ensure test includes all required setup