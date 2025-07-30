#!/usr/bin/env python3
"""
Fix XML comment placement in isolated test files
"""

import os
import re
from pathlib import Path

def fix_xml_comment(file_path):
    """Fix XML comment placement in a single file."""
    
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Check if this is one of the broken files
    if '<!-- Schema Isolated Test' in content and 'xmlns:xsi=' in content:
        # Fix the comment placement
        content = re.sub(
            r'<databaseChangeLog\s*\n?\s*xmlns=.*?\n?\s*xmlns:xsi=.*?\n?\s*<!-- Schema Isolated Test.*?-->\s*\n?\s*xsi:schemaLocation=',
            lambda m: m.group(0).replace('<!-- Schema Isolated Test - No init.xml or cleanup needed -->\n    \n    ', ''),
            content,
            flags=re.DOTALL
        )
        
        # Add comment before databaseChangeLog
        if '<!-- Schema Isolated Test' not in content:
            content = content.replace(
                '<databaseChangeLog',
                '<!-- Schema Isolated Test - No init.xml or cleanup needed -->\n<databaseChangeLog'
            )
        elif not content.startswith('<?xml version="1.0" encoding="UTF-8"?>\n<!-- Schema Isolated Test'):
            # Move comment to correct position
            content = re.sub(
                r'(<?xml version="1.0" encoding="UTF-8"?>\s*)(.*?)(<!-- Schema Isolated Test.*?-->\s*)',
                r'\1\3\2',
                content,
                flags=re.DOTALL
            )
        
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        
        return True
    
    return False

def main():
    """Fix all isolated XML files."""
    
    base_dir = Path(__file__).parent.parent
    snowflake_dir = base_dir / 'src/main/resources/liquibase/harness/change/changelogs/snowflake'
    
    fixed_count = 0
    
    for xml_file in snowflake_dir.glob('*_isolated.xml'):
        if fix_xml_comment(xml_file):
            print(f"Fixed XML comment in {xml_file.name}")
            fixed_count += 1
    
    print(f"\nFixed {fixed_count} files")

if __name__ == '__main__':
    main()