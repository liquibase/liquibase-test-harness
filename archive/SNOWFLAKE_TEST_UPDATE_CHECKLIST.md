# Snowflake Test Harness Update Checklist

## Objective
Update all Snowflake test files to include the full init.xml cleanup as the first changeset.

## Progress Tracker

### Tests to Update (41 total)

| Status | Test File | Has Init? | Updated | Tested |
|--------|-----------|-----------|---------|--------|
| ⏳ | addColumn.xml | TBD | ❌ | ❌ |
| ⏳ | addDefaultValue.xml | TBD | ❌ | ❌ |
| ⏳ | addForeignKey.xml | TBD | ❌ | ❌ |
| ⏳ | addNotNullConstraint.xml | TBD | ❌ | ❌ |
| ⏳ | addPrimaryKey.xml | TBD | ❌ | ❌ |
| ⏳ | addUniqueConstraint.xml | TBD | ❌ | ❌ |
| ⏳ | alterDatabase.xml | TBD | ❌ | ❌ |
| ⏳ | alterSequence.xml | TBD | ❌ | ❌ |
| ⏳ | alterWarehouse.xml | TBD | ❌ | ❌ |
| ⏳ | createDatabase.xml | TBD | ❌ | ❌ |
| ⏳ | createFunction.xml | TBD | ❌ | ❌ |
| ✅ | createOrReplaceWarehouse.xml | YES | ✅ | ❌ |
| ⏳ | createProcedure.xml | TBD | ❌ | ❌ |
| ⏳ | createProcedureFromFile.xml | TBD | ❌ | ❌ |
| ⏳ | createSchemaEnhanced.xml | TBD | ❌ | ❌ |
| ⏳ | createSequence.xml | TBD | ❌ | ❌ |
| ⏳ | createSequenceEnhanced.xml | TBD | ❌ | ❌ |
| ⏳ | createTableDataTypeDoubleIsFloat.xml | TBD | ❌ | ❌ |
| ⏳ | createTableEnhanced.xml | TBD | ❌ | ❌ |
| ⏳ | createView.xml | TBD | ❌ | ❌ |
| ⏳ | createWarehouse.xml | TBD | ❌ | ❌ |
| ✅ | createWarehouseIfNotExists.xml | YES | ✅ | ❌ |
| ✅ | createWarehouseWithResourceConstraint.xml | YES | ✅ | ❌ |
| ⏳ | dropColumn.xml | TBD | ❌ | ❌ |
| ⏳ | dropDatabase.xml | TBD | ❌ | ❌ |
| ⏳ | dropDefaultValue.xml | TBD | ❌ | ❌ |
| ⏳ | dropForeignKey.xml | TBD | ❌ | ❌ |
| ⏳ | dropFunction.xml | TBD | ❌ | ❌ |
| ⏳ | dropProcedure.xml | TBD | ❌ | ❌ |
| ⏳ | dropSequence.xml | TBD | ❌ | ❌ |
| ⏳ | dropTable.xml | TBD | ❌ | ❌ |
| ⏳ | dropView.xml | TBD | ❌ | ❌ |
| ⏳ | dropWarehouse.xml | TBD | ❌ | ❌ |
| ⏳ | modifyDataType.xml | TBD | ❌ | ❌ |
| ⏳ | renameColumn.xml | TBD | ❌ | ❌ |
| ⏳ | renameSequence.xml | TBD | ❌ | ❌ |
| ⏳ | renameView.xml | TBD | ❌ | ❌ |
| ⏳ | setTableRemarks.xml | TBD | ❌ | ❌ |
| ⏳ | sql.xml | TBD | ❌ | ❌ |
| ⏳ | valueSequenceNext.xml | TBD | ❌ | ❌ |

## Summary
- **Total Tests**: 41
- **Already Updated**: 3 (our new WAREHOUSE tests)
- **Need to Check/Update**: 38

## Update Pattern
Each test should start with:
```xml
<!-- Independent test - includes full init cleanup -->
<changeSet author="test-harness" id="cleanup-test-objects" runAlways="true">
    <comment>Clean up test objects from previous runs</comment>
    <sql>
        [Full init.xml SQL content]
    </sql>
</changeSet>
```