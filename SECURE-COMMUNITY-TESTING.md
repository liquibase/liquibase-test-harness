# Secure vs Community Changetype Testing Guide

This document explains how to run tests separated by Liquibase edition (Community vs Secure), which tests support this separation, and the reasoning behind the implementation.

---

## Overview

The Liquibase Test Harness supports running tests filtered by changetype category:
- **Community changetypes**: Free, open-source features (27 changetypes)
- **Secure changetypes**: Paid, enterprise features (34 changetypes)

This separation allows for:
1. Testing Community edition without triggering Secure-only features
2. Validating Secure edition features independently
3. Running comprehensive tests across all changetypes

## Test Mode Options

The `testMode` system property accepts three values:
- **`secure`** - Run Secure (paid) changetype tests only
- **`community`** - Run Community (free) changetype tests only
- **`all`** - Run all tests (Community + Secure combined)

---

## Configuration

All supported changetypes are defined in `supported-changetypes.yml` at the project root:

```yaml
community_changetypes:
  - createTable
  - addColumn
  - addPrimaryKey
  # ... 24 more community changetypes

secure_changetypes:
  - addForeignKey
  - createSequence
  - createFunction
  # ... 31 more secure changetypes
```

Refer to `CHANGETYPE-TESTING.md` for the complete list and detailed categorization.

---

## Supported Tests

The following test categories support Community/Secure separation via the `testMode` system property:

### 1. Change Object Tests
**Helper**: `ChangeObjectTestHelper`

Tests individual object-based changetypes (tables, columns, indexes, constraints, etc.)

```bash
# Run only Community changetype tests
./gradlew test --tests "ChangeObjectTests" -DtestMode=community

# Run only Secure changetype tests
./gradlew test --tests "ChangeObjectTests" -DtestMode=secure

# Run all changetype tests
./gradlew test --tests "ChangeObjectTests" -DtestMode=all
```

**Why supported**: This helper iterates through changelog files per changetype, making it straightforward to filter based on the YAML configuration.

---

### 2. Change Data Tests
**Helper**: `ChangeDataTestHelper`

Tests data manipulation changetypes (insert, delete, loadData, loadUpdateData)

```bash
# Run only Community data changetype tests
./gradlew test --tests "ChangeDataTests" -DtestMode=community

# Run only Secure data changetype tests
./gradlew test --tests "ChangeDataTests" -DtestMode=secure

# Run all data changetype tests
./gradlew test --tests "ChangeDataTests" -DtestMode=all
```

**Why supported**: Data changetypes are clearly separated:
- Community: `insert`, `delete`, `loadData`
- Secure: `loadUpdateData`

The helper filters data-specific changetypes from the YAML configuration.

---

### 3. Snapshot Tests
**Helper**: `SnapshotObjectTestHelper`

Tests snapshot functionality for database objects (tables, views, indexes, sequences, foreign keys)

```bash
# Run only Community snapshot tests
./gradlew test --tests "SnapshotObjectTests" -DtestMode=community

# Run only Secure snapshot tests
./gradlew test --tests "SnapshotObjectTests" -DtestMode=secure

# Run all snapshot tests
./gradlew test --tests "SnapshotObjectTests" -DtestMode=all
```

**Why supported**: Snapshot objects map to their corresponding changetypes:
- Community: `createTable`, `createView`, `createIndex`, `addColumn`, `addPrimaryKey`, `addUniqueConstraint`
- Secure: `createSequence`, `addForeignKeyConstraint`

---

### 4. Generate Changelog Tests
**Helper**: `GenerateChangelogTestHelper`

Tests the `generate-changelog` command for various database objects

```bash
# Run only Community generate-changelog tests
./gradlew test --tests "GenerateChangelogTest" -DtestMode=community

# Run only Secure generate-changelog tests
./gradlew test --tests "GenerateChangelogTest" -DtestMode=secure

# Run all generate-changelog tests
./gradlew test --tests "GenerateChangelogTest" -DtestMode=all
```

**Why supported**: Generate-changelog tests map to specific object types:
- Community: `createTable`, `createView`, `createIndex`, `addColumn`, `addPrimaryKey`, `addUniqueConstraint`, `addCheckConstraint`
- Secure: `createSequence`, `createFunction`, `createProcedure`, `createTrigger`, `createPackage`, `createPackageBody`, `createSynonym`, `addForeignKey`

---

### 5. Advanced Tests
**Helper**: `AdvancedTestHelper`

Tests advanced scenarios combining snapshot, diff, and generate-changelog operations

```bash
# Run only Community advanced tests
./gradlew test --tests "AdvancedTest" -DtestMode=community

# Run only Secure advanced tests
./gradlew test --tests "AdvancedTest" -DtestMode=secure

# Run all advanced tests
./gradlew test --tests "AdvancedTest" -DtestMode=all
```

**Why supported**: Advanced tests iterate through SQL initialization files for different changetypes. The helper maps test names to changetypes:
- Community: `createTable`, `createIndex`, `addCheckConstraint`, `column` (maps to `addColumn`)
- Secure: `createFunction`, `addForeignKeyConstraint` (maps to `addForeignKey`)

---

## Unsupported Tests

### 1. Diff Tests
**Helper**: `DiffCommandTestHelper`

**Why NOT supported**:
- Diff tests use predefined changelog files (e.g., `postgresql14_to_postgresql13.xml`) that contain mixed Community and Secure changetypes in a single file
- Each database pair has a specific changelog with multiple changesets (foreign keys, sequences, functions, procedures, triggers, etc.)
- Separating these would require:
  1. Creating duplicate changelogs for each database pair (e.g., `postgresql14_to_postgresql13_community.xml`, `postgresql14_to_postgresql13_secure.xml`)
  2. Maintaining expected diff outputs for both versions
  3. Complex test logic to handle database-pair-specific scenarios
- **Recommendation**: Keep diff tests as-is for comprehensive cross-version validation

Example of a diff changelog (mixed changetypes):
```xml
<!-- Community changetypes -->
<changeSet id="5" author="as">
    <addColumn tableName="test_table_for_column">...</addColumn>
</changeSet>

<!-- Secure changetypes -->
<changeSet id="4" author="as">
    <addForeignKeyConstraint baseTableName="test_table_base">...</addForeignKeyConstraint>
</changeSet>
<changeSet id="9" author="as">
    <createSequence sequenceName="test_sequence">...</createSequence>
</changeSet>
<changeSet id="13" author="as">
    <pro:createFunction functionName="test_function">...</pro:createFunction>
</changeSet>
```

---

### 2. Foundational Tests
**Helper**: `FoundationalTestHelper`

**Why NOT needed**:
- Only tests basic `createTable` functionality across multiple changelog formats (XML, JSON, YAML, SQL)
- `createTable` is a Community changetype
- The test validates changelog format compatibility, not changetype-specific behavior
- Adding `testMode` filtering would provide no benefit since there's only one test case

---

### 3. Stress Tests
**Helper**: `StressTestHelper`

**Why NOT needed**:
- Focuses on performance and concurrency testing with `createTable` operations (setup, insert, update, select)
- `createTable` is a Community changetype
- The purpose is to test database performance under load, not changetype functionality
- Separation by edition would not enhance the test's value

---

## Implementation Details

### YAML Loading Pattern

All supported test helpers follow this pattern:

```groovy
private static List<String> communityChangetypes
private static List<String> secureChangetypes

static {
    // Load changetypes from the YAML file
    def yamlFile = new File("supported-changetypes.yml")
    if (yamlFile.exists()) {
        Yaml yaml = new Yaml()
        def config = yaml.load(yamlFile.text)
        communityChangetypes = config.community_changetypes
        secureChangetypes = config.secure_changetypes
    } else {
        // Fallback to hardcoded lists if file doesn't exist
        communityChangetypes = [
            'createTable', 'addColumn', // ... etc
        ]
        secureChangetypes = [
            'addForeignKey', 'createSequence', // ... etc
        ]
    }
}
```

### Test Filtering Pattern

```groovy
static List<TestInput> buildTestInput() {
    String testMode = System.getProperty("testMode") // 'secure', 'community', 'all', or null

    List<String> allowedChangetypes = []
    if (testMode == "secure") {
        allowedChangetypes = secureChangetypes
    } else if (testMode == "community") {
        allowedChangetypes = communityChangetypes
    } else {
        // Run all tests - testMode is null, empty, or "all"
        allowedChangetypes = communityChangetypes + secureChangetypes
    }

    // Filter test inputs based on allowedChangetypes
    for (def changeLogEntry : resolvedChangelogs.entrySet()) {
        if (allowedChangetypes.contains(changeLogEntry.key)) {
            inputList.add(TestInput.builder()...build())
        }
    }
}
```

### Fallback Strategy

Each helper includes hardcoded fallback lists to ensure tests continue working even if:
1. The `supported-changetypes.yml` file is missing
2. The YAML file is corrupted or invalid
3. The project structure changes

This provides resilience and backward compatibility.

---

## Running Tests in CI/CD

### Community Edition Validation
```yaml
- name: Test Community Features
  run: |
    ./gradlew test --tests "ChangeObjectTests" -DtestMode=community
    ./gradlew test --tests "ChangeDataTests" -DtestMode=community
    ./gradlew test --tests "SnapshotObjectTests" -DtestMode=community
    ./gradlew test --tests "GenerateChangelogTest" -DtestMode=community
    ./gradlew test --tests "AdvancedTest" -DtestMode=community
```

### Secure Edition Validation
```yaml
- name: Test Secure Features
  run: |
    ./gradlew test --tests "ChangeObjectTests" -DtestMode=secure
    ./gradlew test --tests "ChangeDataTests" -DtestMode=secure
    ./gradlew test --tests "SnapshotObjectTests" -DtestMode=secure
    ./gradlew test --tests "GenerateChangelogTest" -DtestMode=secure
    ./gradlew test --tests "AdvancedTest" -DtestMode=secure
```

### Full Validation
```yaml
- name: Test All Features
  run: |
    ./gradlew test --tests "ChangeObjectTests" -DtestMode=all
    ./gradlew test --tests "ChangeDataTests" -DtestMode=all
    ./gradlew test --tests "SnapshotObjectTests" -DtestMode=all
    ./gradlew test --tests "GenerateChangelogTest" -DtestMode=all
    ./gradlew test --tests "AdvancedTest" -DtestMode=all
    ./gradlew test --tests "DiffTest"
    ./gradlew test --tests "FoundationalTest"
    ./gradlew test --tests "StressTest"
```

---

## Maintenance

### Adding New Changetypes

When adding a new changetype to Liquibase:

1. Update `supported-changetypes.yml`:
   ```yaml
   community_changetypes:
     - newCommunityChangetype

   secure_changetypes:
     - newSecureChangetype
   ```

2. Update the fallback lists in each test helper's static block

3. Create test resources (changelogs, expected SQL, etc.) following existing patterns

4. The test helpers will automatically pick up the new changetypes

### Changing Changetype Categories

If a changetype moves from Community to Secure (or vice versa):

1. Move it in `supported-changetypes.yml`
2. Update fallback lists in test helpers
3. Update `CHANGETYPE-TESTING.md` documentation
4. Update the counters in `supported-changetypes.yml`

---

## Summary Table

| Test Category | Helper Class | Supports Separation | Reason |
|--------------|--------------|---------------------|---------|
| Change Object Tests | `ChangeObjectTestHelper` | ✅ Yes | Iterates through changelog files per changetype |
| Change Data Tests | `ChangeDataTestHelper` | ✅ Yes | Clear separation of data changetypes |
| Snapshot Tests | `SnapshotObjectTestHelper` | ✅ Yes | Snapshot objects map to changetypes |
| Generate Changelog | `GenerateChangelogTestHelper` | ✅ Yes | Object types map to changetypes |
| Advanced Tests | `AdvancedTestHelper` | ✅ Yes | Test files map to changetypes |
| Diff Tests | `DiffCommandTestHelper` | ❌ No | Mixed changelogs per database pair |
| Foundational Tests | `FoundationalTestHelper` | ⚪ N/A | Only tests community createTable |
| Stress Tests | `StressTestHelper` | ⚪ N/A | Performance testing only |

---

## Questions & Support

For questions about this testing structure or to report issues:
- See `CHANGETYPE-TESTING.md` for detailed changetype categorization
- See `supported-changetypes.yml` for the complete changetype list
- Report issues at https://github.com/liquibase/liquibase-test-harness/issues
