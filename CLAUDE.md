# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Liquibase Test Harness

## Repository Overview

**liquibase-test-harness** is a comprehensive integration test framework for validating Liquibase functionality across 30+ database platforms and versions. It's used to ensure database-specific interactions within Liquibase work correctly against different database versions and configurations.

**URL**: https://github.com/liquibase/liquibase-test-harness
**License**: FSL-1.1-ALv2 (Functional Source License)
**Build Tool**: Maven (Java 17+)
**Test Framework**: Groovy + Spock + JUnit 6

---

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.5+
- Docker & Docker Compose (for local testing)

### Build
```bash
mvn clean install -DskipTests
```

### Run All Tests
```bash
mvn test
```

### Run Specific Test Suite
```bash
mvn test -Dtest=LiquibaseHarnessSuiteTest
mvn test -Dtest=FoundationalHarnessSuiteTest
mvn test -Dtest=AdvancedHarnessSuiteTest
```

### Run Tests with Options
```bash
# Specific database
mvn test -DdbName=postgresql -DdbVersion=14

# Specific change object
mvn test -DchangeObjects=createTable,dropTable

# Specific input format (xml, sql, json, yml, yaml, all, all-structured)
mvn test -DinputFormat=all

# Filter by platform prefix
mvn test -Dprefix=docker
```

### Local Docker Setup
```bash
cd src/test/resources/docker
docker compose up -d
# Wait for databases to start
mvn test
docker compose down --volumes
```

---

## Architecture & Key Components

### Test Suites (3 Types)

1. **BaseHarnessSuite** (ChangeObjectTests + ChangeDataTests)
   - Tests DDL/DML change types
   - Validates SQL generation and snapshot changes
   - Uses changelog files to test create/drop/alter operations

2. **FoundationalHarnessSuite** (FoundationalTest)
   - Tests basic Liquibase commands: validate, update, tag, rollback, status, history
   - Uses changelog formats: XML, YAML, JSON, SQL
   - Tests metadata (labels, contexts, comments) in DATABASECHANGELOG table

3. **AdvancedHarnessSuite** (AdvancedTest)
   - Tests: snapshot, generateChangelog, diffChangelog, diff commands
   - Database-specific change type validation
   - Uses two database instances (primary & secondary) for diff testing

### Core Test Classes

| Class | Location | Purpose |
|-------|----------|---------|
| ChangeObjectTests | src/main/groovy/liquibase/harness/change | Tests change types (DDL) |
| ChangeDataTests | src/main/groovy/liquibase/harness/data | Tests data operations (DML) |
| FoundationalTest | src/main/groovy/liquibase/harness/compatibility/foundational | Tests basic Liquibase workflow |
| AdvancedTest | src/main/groovy/liquibase/harness/compatibility/advanced | Tests generateChangelog/diff/snapshot |
| SnapshotObjectTests | src/main/groovy/liquibase/harness/snapshot | Tests snapshot command |

### Utility Classes

- **TestConfig.groovy**: Loads harness-config.yml, manages database configuration
- **DatabaseUnderTest.groovy**: POJO for database connection info
- **DatabaseConnectionUtil.groovy**: Creates/manages JDBC connections
- **DatabaseTestContext.groovy**: Singleton managing connection caching and lifecycle
- **FileUtils.groovy**: File/resource discovery and filtering
- **JSONUtils.groovy**: JSON snapshot comparison and validation
- **TestUtils.groovy**: Common test utilities
- **RollbackStrategy**: Abstract base for rollback implementations (RollbackByTag, RollbackToDate)

---

## Directory Structure

```
src/
├── main/
│   ├── groovy/liquibase/harness/
│   │   ├── change/           # ChangeObjectTests & helpers
│   │   ├── data/             # ChangeDataTests & helpers
│   │   ├── snapshot/         # SnapshotObjectTests & helpers
│   │   ├── compatibility/
│   │   │   ├── foundational/ # FoundationalTest
│   │   │   └── advanced/     # AdvancedTest
│   │   ├── config/           # TestConfig, DatabaseUnderTest
│   │   ├── util/             # Utility classes
│   │   └── BaseHarnessSuite.groovy
│   └── resources/liquibase/harness/
│       ├── change/
│       │   ├── changelogs/           # Input: changelog files per database
│       │   ├── expectedSql/          # Expected SQL per database
│       │   └── expectedSnapshot/     # Expected DB state per database
│       ├── data/
│       │   ├── changelogs/           # DML changesets
│       │   ├── checkingSql/          # Verification queries
│       │   ├── expectedSql/          # Expected DML SQL
│       │   └── expectedResultSet/    # Expected result sets (JSON)
│       ├── compatibility/
│       │   ├── foundational/
│       │   │   ├── changelogs/
│       │   │   ├── checkingSql/
│       │   │   └── expectedResultSet/
│       │   └── advanced/
│       │       ├── initSql/          # SQL to create test objects
│       │       ├── expectedSql/
│       │       ├── expectedSnapshot/
│       │       └── expectedDiff/
│       ├── diff/
│       │   ├── changelogs/
│       │   └── expectedDiff/
│       ├── generateChangelog/
│       │   ├── expectedChangeLog/
│       │   └── expectedSql/
│       ├── snapshot/
│       │   ├── changelogs/
│       │   └── expectedSnapshot/
│       └── stress/                   # Performance test data
├── test/
│   ├── groovy/liquibase/harness/
│   │   ├── FoundationalTest.groovy
│   │   ├── AdvancedTest.groovy
│   │   ├── ChangeObjectTests.groovy
│   │   ├── ChangeDataTests.groovy
│   │   ├── SnapshotObjectTests.groovy
│   │   ├── GenerateChangelogTest.groovy
│   │   ├── DiffTest.groovy
│   │   ├── DiffChangelogTest.groovy
│   │   ├── LiquibaseHarnessSuiteTest.groovy
│   │   └── *HarnessSuiteTest.groovy
│   └── resources/
│       ├── harness-config.yml        # Configuration (databases to test)
│       ├── docker/
│       │   ├── docker-compose.yml    # 30+ database services
│       │   ├── mysql-init.sql
│       │   ├── postgres-init.sql
│       │   └── [database specific inits]
│       ├── H2/                       # H2 embedded database files
│       ├── sqlite/                   # SQLite database files
│       └── derby/                    # Derby database files
```

---

## Test Configuration

### harness-config.yml Structure

Located at `src/test/resources/harness-config.yml`. Example entry:

```yaml
inputFormat: xml              # Default format: xml, sql, json, yml, yaml
context: testContext          # Liquibase context applied to changesets

databasesUnderTest:
  - name: postgresql          # Database identifier (maps to folders)
    prefix: docker            # Filter group (docker, cloud, local)
    version: 14               # Version for version-specific folders
    url: jdbc:postgresql://localhost:5438/lbcat
    username: lbuser
    password: LiquibasePass1
  
  - name: mysql
    prefix: docker
    version: 8
    url: jdbc:mysql://localhost:33061/lbcat
    username: lbuser
    password: LiquibasePass1
```

**Prefix** values for filtering:
- `docker`: Local Docker containers
- `cloud`: Cloud database instances (AWS, Azure, GCP, OCI)
- `local`: Embedded databases (H2, SQLite)

### Test Configuration Inheritance

Files/directories follow a **database-specific hierarchy**:

1. **Root level** (applies to all databases)
   - `src/main/resources/liquibase/harness/change/changelogs`
   - `src/main/resources/liquibase/harness/change/expectedSql`

2. **Database-specific** (applies only to that database)
   - `src/main/resources/liquibase/harness/change/changelogs/postgresql`
   - `src/main/resources/liquibase/harness/change/expectedSql/postgresql`

3. **Version-specific** (applies only to that version)
   - `src/main/resources/liquibase/harness/change/changelogs/postgresql/14`
   - `src/main/resources/liquibase/harness/change/expectedSql/postgresql/14`

---

## Test Execution Flow

### ChangeObjectTest Flow (Example)
```
1. Load changelog from:
   - changelogs/{database}/{version}/*.xml  (version-specific)
   - changelogs/{database}/*.xml            (database-specific)
   - changelogs/*.xml                       (global)

2. Run Liquibase updateSql to generate SQL
3. Compare against expectedSql/{database}/{version}/{changename}.sql

4. If SQL matches, run Liquibase update on test database

5. Take database snapshot via Liquibase snapshot command

6. Compare snapshot against expectedSnapshot/{database}/{version}/{changename}.json

7. Rollback changeset (using tag or date strategy)

8. Verify rollback via second snapshot
```

### DatabaseUnderTest Filtering

```groovy
// Filter by multiple criteria
List<DatabaseUnderTest> databases = TestConfig.getInstance()
    .getFilteredDatabasesUnderTest()

// System properties used for filtering:
-DdbName=postgresql          // Filter by database name
-DdbVersion=14               // Filter by version
-Dprefix=docker              // Filter by prefix (docker, cloud, local)
-DdbUsername=override        // Override username
-DdbPassword=override        // Override password
-DdbUrl=jdbc:...             // Override JDBC URL
-DconfigFile=custom.yml      // Use custom config file
```

---

## Key Features & Patterns

### 1. Multi-Format Support
Tests run against:
- **XML**: Default, most complete
- **SQL**: Direct SQL scripts
- **JSON**: Structured change definitions
- **YAML**: Human-readable format

Run all with: `-DinputFormat=all`

### 2. Database Hierarchy
```
postgresql/14/createTable.xml       (version-specific)
postgresql/createTable.xml          (database-specific)
createTable.xml                     (global/default)
```
Version > Database > Global precedence

### 3. Rollback Strategies
Two strategies supported (set via `-DrollbackStrategy`):

- **rollbackToDate** (default)
  - Creates UTC timestamp before update
  - Rolls back to that point
  - Works best for UTC-aligned environments

- **rollbackByTag**
  - Uses "test-harness-tag" tag
  - Better for cloud databases in different timezones
  - Requires explicit tag in changelog

### 4. Snapshot Verification
- Uses Liquibase snapshot command to capture DB state
- Compares JSON snapshots: actual vs expected
- Supports `_noMatch: true` to verify object absence
- Supports `_noMatchField` to verify specific property differences

### 5. Skipping Tests
Three mechanisms for skipping:

**a) Individual Skip File:**
```
src/main/resources/liquibase/harness/change/expectedSql/{db}/createTable.sql

Content: "SKIP TEST" or "INVALID TEST"
```

**b) Bulk Skip File:**
```
src/main/resources/liquibase/harness/change/expectedSql/{db}/skipChangetypes.txt

Content:
addAutoIncrement
addForeignKey
createTrigger
```

**c) Exclude All Defaults:**
```
Create marker file:
src/main/resources/liquibase/harness/change/changelogs/{db}/excludeDefaultChangelogs

Forces use of database-specific changelogs only
```

### 6. Placeholders in expectedSql
- `${CATALOG_NAME}`: Default catalog name
- `${SCHEMA_NAME}`: Default schema name

Example:
```sql
ALTER TABLE ${CATALOG_NAME}.${SCHEMA_NAME}.authors ADD column1 VARCHAR(25)
```

### 7. Result Set Verification (ChangeDataTests)
- Runs DML changesets
- Executes verification query from checkingSql
- Compares JSON result set against expectedResultSet
- Empty `{}` skips comparison

---

## Maven Build & Plugins

### Key Dependencies
```xml
<properties>
  <liquibase-core.version>5.0.2</liquibase-core.version>
  <liquibase-commercial.version>5.1.1</liquibase-commercial.version>
  <groovy-all.version>5.0.4</groovy-all.version>
  <spock-core.version>2.4-groovy-5.0</spock-core.version>
  <junit.version>6.0.3</junit.version>
</properties>
```

### Key Plugins
- **gmavenplus-plugin**: Compiles Groovy with Java integration
- **maven-surefire-plugin**: Runs tests (Spock/JUnit specs)
- **maven-failsafe-plugin**: Integration tests
- **maven-javadoc-plugin**: Generates documentation

### Test Execution
Surefire plugin includes:
- `**/*Test` (JUnit convention)
- `**/*Tests` (Plural convention)
- `**/*Spec` (Spock convention)
- `**/*Suite` (Test suite convention)

### Profiles
```bash
# Community artifacts (default)
mvn clean install -DskipTests

# Pro artifacts (Liquibase Secure)
mvn clean install -DskipTests -Puseproartifacts -DuseProArtifacts=true
```

---

## GitHub Actions Workflows

Located in `.github/workflows/`:

### Main Workflow (main.yml)
- **Trigger**: Push, PR, manual dispatch, daily schedule (6am UTC)
- **Smart Artifact Selection**: 
  - From `liquibase/liquibase` (community) if triggered there
  - From `liquibase/liquibase-pro` (pro) if triggered there
- **Databases**: 30+ via docker-compose
- **Manual Inputs**:
  - `liquibaseBranch`: Branch to pull from (fallback support)
  - `liquibaseCommit`: Specific commit SHA
  - `liquibaseRepo`: Force specific repository
  - `testClasses`: Select test suite
  - `databases`: JSON array of database names

### Advanced Workflow (advanced.yml)
- **Trigger**: Manual dispatch, scheduled
- **Artifacts**: Always uses `liquibase/liquibase-pro` (pro)
- **Tests**: AdvancedHarnessSuite, FoundationalHarnessSuite
- **Purpose**: Extended compatibility testing

### Cloud Workflows
- **aws.yml**: AWS RDS testing (MySQL, PostgreSQL, Oracle, SQL Server, MariaDB)
- **azure.yml**: Azure SQL DB, MySQL, PostgreSQL, SQL MI
- **gcp.yml**: GCP PostgreSQL, MySQL, SQL Server
- **oracle-oci.yml**: Oracle OCI instances
- **snowflake.yml**: Snowflake cloud data warehouse

### Parallel Execution
- **OracleRunParallel.yml**: Runs Oracle tests in parallel across versions

### Artifact Resolution
- Uses Maven for dependency resolution
- Queries GitHub Packages maven-metadata.xml for versions
- Community: `{commit-sha}-SNAPSHOT` format
- Pro: `main-{short-sha}` format
- Automatic fallback to main if branch not found

---

## Database Support

**30+ Platforms** across 3 verification levels:

### Advanced (Full Testing)
- PostgreSQL (12-16)
- MySQL (5.6-8)
- MariaDB (10.2-11.4)
- SQL Server (2017, 2019, 2022)
- Oracle (18.3, 18.4, 19.0, 21.3)
- CockroachDB (23.1-24.1)
- H2, SQLite, Firebird, HSQLDB
- EDB PostgreSQL, EDB EDB
- Snowflake, Azure SQL DB, AWS RDS variants

### BaseHarnessSuite (Core Testing)
- Snowflake
- Azure SQL Managed Instance
- DB2 z/OS

### Deprecated
- Apache Derby (read-only maintenance, requires Java 21)

**Note**: Version folder names must match exactly with DB version in `harness-config.yml`

---

## Running Tests Locally

### Quick Start (One Database)
```bash
# Pull latest Docker image
docker pull postgres:16

# Start one service
cd src/test/resources/docker
docker-compose up -d postgres-16

# Run tests against it
mvn test -DdbName=postgresql -DdbVersion=16

# Cleanup
docker-compose down -v postgres-16
```

### Run All Local Tests
```bash
cd src/test/resources/docker
docker-compose up -d

# Wait for all services to be healthy (check logs)
docker-compose logs -f

# In new terminal:
mvn test

# Cleanup
docker-compose down --volumes
```

### Custom Config File
```bash
mvn test -DconfigFile=custom-harness-config.yml
```

### Test Only Specific Changes
```bash
# Single change object
mvn test -DchangeObjects=createTable

# Multiple
mvn test -DchangeObjects=createTable,dropTable,addColumn

# Data operations
mvn test -DchangeData=insert,delete

# Advanced test specific change
mvn test -Dchange=createTable
```

### Test Formats
```bash
# XML only (default)
mvn test

# All formats
mvn test -DinputFormat=all

# Structured formats only (xml, json, yml, yaml)
mvn test -DinputFormat=all-structured

# SQL only
mvn test -DinputFormat=sql
```

---

## Common Issues & Troubleshooting

### Connection Refused
- Check docker containers are running: `docker ps`
- Check port mappings in `harness-config.yml` vs docker-compose.yml
- Verify firewall rules

### Test Isolation Issues
- Tests use rollback strategies to clean up
- Check `-DrollbackStrategy=rollbackByTag` for cloud databases
- Verify DATABASECHANGELOG table exists

### Memory Issues
- Increase heap: `MAVEN_OPTS=-Xmx2g mvn test`

### Schema/Catalog Issues
- Different DBs handle schemas differently
- H2/SQLite use file-based storage
- Oracle uses schemas within tablespaces
- Snowflake uses fully-qualified names with catalog

### Skipped Tests
- Check if database/version in config
- Check expectedSql contains "SKIP TEST"
- Check skipChangetypes.txt for bulk skips

---

## Adding New Tests

### Add a Change Object Test
1. Create changelog: `src/main/resources/liquibase/harness/change/changelogs/{database}/{version}/myChange.xml`
2. Create expected SQL: `src/main/resources/liquibase/harness/change/expectedSql/{database}/{version}/myChange.sql`
3. Create expected snapshot: `src/main/resources/liquibase/harness/change/expectedSnapshot/{database}/{version}/myChange.json`
4. Run test: `mvn test -DchangeObjects=myChange`

### Add a Data Test
1. Create DML changelog: `src/main/resources/liquibase/harness/data/changelogs/{database}/myDataOp.xml`
2. Create checking query: `src/main/resources/liquibase/harness/data/checkingSql/{database}/myDataOp.sql`
3. Create expected result set: `src/main/resources/liquibase/harness/data/expectedResultSet/{database}/myDataOp.json`
4. Run: `mvn test -DchangeData=myDataOp`

### Add Advanced Test
1. Create init SQL: `src/main/resources/liquibase/harness/compatibility/advanced/initSql/primary/myChange.sql`
2. Create secondary init: `src/main/resources/liquibase/harness/compatibility/advanced/initSql/secondary/myChange.sql`
3. Create expected SQL: `src/main/resources/liquibase/harness/compatibility/advanced/expectedSql/generateChangelog/myChange.sql`
4. Create expected snapshot: `src/main/resources/liquibase/harness/compatibility/advanced/expectedSnapshot/myChange.json`
5. Create expected diff: `src/main/resources/liquibase/harness/compatibility/advanced/expectedDiff/myChange.txt`

---

## Integration with Extensions

This framework is designed for use in Liquibase extensions. Extensions can:

1. Add as test dependency in their pom.xml
2. Create database-specific test data folders
3. Leverage existing test infrastructure
4. Verify extension functionality doesn't break core features
5. Add custom test suites that extend BaseHarnessSuite

See `README.extensions.md` for detailed extension integration guide.

---

## Key Files & Locations

| File | Purpose |
|------|---------|
| pom.xml | Maven build configuration, dependencies, plugins |
| src/test/resources/harness-config.yml | Database connection configuration |
| src/test/resources/docker/docker-compose.yml | 30+ database service definitions |
| src/main/groovy/liquibase/harness/ | Core test framework code |
| src/main/resources/liquibase/harness/ | Test data (changelogs, expected outputs) |
| .github/workflows/main.yml | Primary CI/CD workflow |
| README.md | Comprehensive user documentation |
| README.advanced-test.md | Advanced test specifics |
| README.extensions.md | Extension integration guide |
| README.localstack.md | LocalStack AWS testing guide |

---

## Development Notes

### Code Style
- Written in Groovy with Java interop
- Spock testing framework conventions
- Singleton pattern for TestConfig, DatabaseTestContext

### Connection Management
- DatabaseTestContext caches JDBC connections
- Lazy initialization, graceful shutdown
- Automatic cleanup on JVM shutdown hooks

### Resource Loading
- ClassLoaderResourceAccessor for test data
- Version/database hierarchy for file discovery
- Recursive directory scanning with filtering

### Error Handling
- Test failures don't stop suite execution
- Connection errors logged but don't block subsequent tests
- Rollback failures are non-fatal (logged)

---

## References

- **Homepage**: https://www.liquibase.org
- **Documentation**: https://docs.liquibase.com
- **Change Types**: https://docs.liquibase.com/change-types/home.html
- **Repository**: https://github.com/liquibase/liquibase-test-harness
- **Issues**: https://github.com/liquibase/liquibase-test-harness/issues
