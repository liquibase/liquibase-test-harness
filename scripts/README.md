# Test Harness Utility Scripts

This directory contains utility scripts used during test harness development and migration.

## Scripts

### convert_to_schema_isolation.py
- **Purpose**: Converts existing Snowflake tests to use the new schema isolation pattern
- **Usage**: One-time migration script for updating test files
- **Status**: Migration complete - kept for reference

### copy_expected_files.py
- **Purpose**: Utility to copy expected SQL/snapshot files between test variations
- **Usage**: Helper for creating new test variations
- **Status**: Can be useful for future test development

### fix_xml_comments.py
- **Purpose**: Fixes XML comment formatting in test files
- **Usage**: Code cleanup utility
- **Status**: Can be useful for maintaining consistent formatting

## Note
These scripts are development utilities and are not part of the test harness runtime.
They can be safely ignored for normal test execution.