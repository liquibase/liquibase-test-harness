#!/usr/bin/env python3
"""
Convert Snowflake test harness tests to use schema isolation pattern.
Removes init.xml includes, rollback blocks, and cleanup changesets.
"""

import os
import re
import xml.etree.ElementTree as ET
from pathlib import Path
import shutil

# Tests that should use schema isolation
TESTS_TO_CONVERT = [
    # Table Operations
    'createTableEnhanced.xml',
    'createTableDataTypeDoubleIsFloat.xml',
    'dropTable.xml',
    'renameTable.xml',
    'alterTable.xml',
    'alterTableCluster.xml',
    'setTableRemarks.xml',
    
    # Column Operations
    'addColumn.xml',
    'dropColumn.xml',
    'renameColumn.xml',
    'modifyDataType.xml',
    
    # Constraint Operations
    'addPrimaryKey.xml',
    'addForeignKey.xml',
    'addUniqueConstraint.xml',
    'addNotNullConstraint.xml',
    'addDefaultValue.xml',
    'dropForeignKey.xml',
    'dropDefaultValue.xml',
    'createPrimaryKeyConstraint.xml',
    'createForeignKeyConstraint.xml',
    
    # View Operations
    'createView.xml',
    'dropView.xml',
    'renameView.xml',
    
    # Sequence Operations
    'createSequence.xml',
    'createSequenceEnhanced.xml',
    'dropSequence.xml',
    'dropSequenceSimple.xml',
    'dropSequenceWithCascade.xml',
    'dropSequenceWithRestrict.xml',
    'alterSequence.xml',
    'alterSequenceWithNoOrder.xml',
    'renameSequence.xml',
    'valueSequenceNext.xml',
    
    # Procedure/Function Operations
    'createProcedure.xml',
    'createProcedureFromFile.xml',
    'dropProcedure.xml',
    'createFunction.xml',
    'dropFunction.xml',
    
    # Other
    'sql.xml',
    'testNamespacedAttributes.xml',
    'testUnsetOnly.xml'
]

def clean_xml_content(content):
    """Remove init.xml includes, rollback blocks, and cleanup changesets."""
    
    # Remove include tags for init.xml
    content = re.sub(r'<include\s+file="[^"]*init\.xml"\s*/>\s*\n?', '', content)
    content = re.sub(r'<!--\s*Include init\.xml.*?-->\s*\n?', '', content, flags=re.IGNORECASE)
    
    # Remove rollback blocks (but keep the content if needed)
    content = re.sub(r'<rollback>.*?</rollback>\s*\n?', '', content, flags=re.DOTALL)
    
    # Remove cleanup changesets
    # Match changesets with IDs containing 'cleanup'
    content = re.sub(
        r'<changeSet[^>]*id="[^"]*cleanup[^"]*"[^>]*>.*?</changeSet>\s*\n?',
        '',
        content,
        flags=re.DOTALL | re.IGNORECASE
    )
    
    # Add comment about schema isolation
    if '<!-- Schema Isolated Test' not in content:
        content = content.replace(
            '<databaseChangeLog',
            '<!-- Schema Isolated Test - No init.xml or cleanup needed -->\n<databaseChangeLog'
        )
    
    # Clean up extra blank lines
    content = re.sub(r'\n\s*\n\s*\n', '\n\n', content)
    
    return content

def convert_file(input_path, output_path):
    """Convert a single test file to use schema isolation."""
    
    print(f"Converting {input_path.name}...")
    
    # Read the original content
    with open(input_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Clean the content
    cleaned_content = clean_xml_content(content)
    
    # Write to new file
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(cleaned_content)
    
    print(f"  Created {output_path.name}")

def main():
    """Main conversion process."""
    
    # Set up paths
    base_dir = Path(__file__).parent.parent
    snowflake_dir = base_dir / 'src/main/resources/liquibase/harness/change/changelogs/snowflake'
    
    if not snowflake_dir.exists():
        print(f"Error: Directory not found: {snowflake_dir}")
        return
    
    # Create backup directory
    backup_dir = snowflake_dir / 'backup_before_isolation'
    backup_dir.mkdir(exist_ok=True)
    
    converted_count = 0
    
    for test_file in TESTS_TO_CONVERT:
        input_path = snowflake_dir / test_file
        
        if not input_path.exists():
            print(f"Warning: File not found: {test_file}")
            continue
        
        # Create backup
        backup_path = backup_dir / test_file
        if not backup_path.exists():
            shutil.copy2(input_path, backup_path)
        
        # Convert to new name (append _isolated)
        output_name = test_file.replace('.xml', '_isolated.xml')
        output_path = snowflake_dir / output_name
        
        # Convert the file
        convert_file(input_path, output_path)
        converted_count += 1
    
    print(f"\nConversion complete!")
    print(f"Converted {converted_count} files")
    print(f"Backups saved in: {backup_dir}")
    print("\nTo use schema isolation, run tests with:")
    print("  -Dliquibase.harness.lifecycle.enabled=true")
    print("  -Dliquibase.harness.lifecycle.schemaIsolation=true")

if __name__ == '__main__':
    main()