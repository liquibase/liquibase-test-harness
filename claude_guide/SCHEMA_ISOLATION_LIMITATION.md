# Schema Isolation Limitation Discovery

## Key Finding

**Liquibase ignores `USE SCHEMA` commands when generating SQL**. It always uses the default schema from the database connection.

## What We Tried

```xml
<changeSet id="setup" runAlways="true">
    <sql>CREATE SCHEMA TEST_CREATE_SEQUENCE</sql>
    <sql>USE SCHEMA TEST_CREATE_SEQUENCE</sql>
</changeSet>

<changeSet id="test">
    <createSequence sequenceName="test_sequence"/>
</changeSet>
```

## What Happened

Expected SQL:
```sql
CREATE SEQUENCE TEST_CREATE_SEQUENCE.test_sequence
```

Actual SQL:
```sql
CREATE SEQUENCE TESTHARNESS.test_sequence
```

## Why This Happens

1. Liquibase uses the **default schema** from the JDBC connection
2. The connection is configured with `schema: TESTHARNESS` 
3. `USE SCHEMA` is a session-level command that doesn't affect Liquibase's SQL generation
4. Liquibase generates SQL with explicit schema prefixes based on the connection default

## Implications

### Can't Use Schema Isolation With:
- Liquibase change types (`<createTable>`, `<createSequence>`, etc.)
- These will always use the connection's default schema

### Can Use Schema Isolation With:
- Raw SQL commands that don't specify schema
- Custom SQL that explicitly uses the schema name

## Alternative Approaches

### 1. Unique Object Names (Recommended)
```xml
<changeSet id="test-createSequence">
    <createSequence sequenceName="CREATESEQ_test_sequence"/>
</changeSet>

<changeSet id="test-alterSequence">
    <createSequence sequenceName="ALTERSEQ_test_sequence"/>
</changeSet>
```

### 2. Raw SQL with Schema Control
```xml
<changeSet id="setup">
    <sql>CREATE SCHEMA TEST_SEQUENCES</sql>
</changeSet>

<changeSet id="test">
    <sql>CREATE SEQUENCE TEST_SEQUENCES.test_sequence</sql>
</changeSet>
```

### 3. Multiple Database Connections
Configure separate connections with different default schemas (complex, not recommended for test harness)

## Recommendation

**Use unique object naming** for test isolation:
- Simpler to implement
- Works with all Liquibase change types
- No schema management overhead
- Clear which test owns which object
- Allows parallel execution

## Example Pattern

```xml
<!-- Self-contained test with unique names -->
<changeSet id="cleanup" runAlways="true">
    <sql>DROP SEQUENCE IF EXISTS TESTNAME_sequence</sql>
</changeSet>

<changeSet id="test">
    <createSequence sequenceName="TESTNAME_sequence"/>
    <rollback>
        <dropSequence sequenceName="TESTNAME_sequence"/>
    </rollback>
</changeSet>
```