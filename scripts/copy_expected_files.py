#!/usr/bin/env python3
"""
Copy expected files for schema isolated tests
"""

import shutil
from pathlib import Path

# Tests that were converted to isolated versions
ISOLATED_TESTS = [
    'addColumn',
    'createTableEnhanced',
    'createTableDataTypeDoubleIsFloat',
    'dropTable',
    'renameTable',
    'alterTable',
    'alterTableCluster',
    'setTableRemarks',
    'dropColumn',
    'renameColumn',
    'modifyDataType',
    'addPrimaryKey',
    'addForeignKey',
    'addUniqueConstraint',
    'addNotNullConstraint',
    'addDefaultValue',
    'dropForeignKey',
    'dropDefaultValue',
    'createPrimaryKeyConstraint',
    'createForeignKeyConstraint',
    'createView',
    'dropView',
    'renameView',
    'createSequence',
    'createSequenceEnhanced',
    'dropSequence',
    'dropSequenceSimple',
    'dropSequenceWithCascade',
    'dropSequenceWithRestrict',
    'alterSequence',
    'alterSequenceWithNoOrder',
    'renameSequence',
    'valueSequenceNext',
    'createProcedure',
    'createProcedureFromFile',
    'dropProcedure',
    'createFunction',
    'dropFunction',
    'sql',
    'testNamespacedAttributes',
    'testUnsetOnly'
]

def main():
    base_dir = Path(__file__).parent.parent
    
    # Copy expectedSnapshot files
    snapshot_dir = base_dir / 'src/main/resources/liquibase/harness/change/expectedSnapshot/snowflake'
    for test in ISOLATED_TESTS:
        original = snapshot_dir / f'{test}.json'
        isolated = snapshot_dir / f'{test}_isolated.json'
        
        if original.exists() and not isolated.exists():
            shutil.copy2(original, isolated)
            print(f"Copied {original.name} -> {isolated.name}")
    
    # Copy expectedSql files
    sql_dir = base_dir / 'src/main/resources/liquibase/harness/change/expectedSql/snowflake'
    for test in ISOLATED_TESTS:
        original = sql_dir / f'{test}.sql'
        isolated = sql_dir / f'{test}_isolated.sql'
        
        if original.exists() and not isolated.exists():
            shutil.copy2(original, isolated)
            print(f"Copied {original.name} -> {isolated.name}")

if __name__ == '__main__':
    main()