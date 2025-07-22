# Claude AI - Liquibase Test Harness Knowledge Base

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

## Quick Reference

### Adding a New Test
1. Create `changelogs/snowflake/myTest.xml` with setup + test
2. Create `expectedSql/snowflake/myTest.sql` with ALL SQL
3. Create `expectedSnapshot/snowflake/myTest.json` with expected state
4. Update `init.xml` to drop any new objects
5. Run: `mvn test -Dtest=ChangeObjectTests -DchangeObjects=init,myTest -DdbName=snowflake`

### Debugging Failed Tests
1. Check if init is running (look for cleanup SQL in logs)
2. Verify all three expected files exist
3. Compare generated vs expected SQL character by character
4. Check for "previously run" errors indicating dirty state
5. Ensure test includes all required setup