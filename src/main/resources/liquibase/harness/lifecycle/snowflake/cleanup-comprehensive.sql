-- Snowflake Test Harness Comprehensive Cleanup Script
-- This script runs after each test to ensure clean state

-- Clear DATABASECHANGELOG entries (except for init.xml)
DELETE FROM DATABASECHANGELOG WHERE FILENAME NOT LIKE '%init.xml';

-- Ensure DATABASECHANGELOGLOCK is released
UPDATE DATABASECHANGELOGLOCK SET LOCKED = FALSE WHERE ID = 1;

-- Drop all test tables (common patterns)
BEGIN
    LET tables_cursor CURSOR FOR
        SELECT TABLE_NAME 
        FROM INFORMATION_SCHEMA.TABLES 
        WHERE TABLE_SCHEMA = CURRENT_SCHEMA()
        AND TABLE_NAME NOT IN ('DATABASECHANGELOG', 'DATABASECHANGELOGLOCK')
        AND (
            TABLE_NAME LIKE 'TEST_%' OR 
            TABLE_NAME LIKE 'TMP_%' OR
            TABLE_NAME LIKE 'AUTHORS%' OR
            TABLE_NAME LIKE 'POSTS%' OR
            TABLE_NAME LIKE 'EXAMPLE_%'
        );
    
    FOR record IN tables_cursor DO
        EXECUTE IMMEDIATE 'DROP TABLE IF EXISTS ' || record.TABLE_NAME || ' CASCADE';
    END FOR;
END;

-- Drop all test views
BEGIN
    LET views_cursor CURSOR FOR
        SELECT TABLE_NAME 
        FROM INFORMATION_SCHEMA.VIEWS 
        WHERE TABLE_SCHEMA = CURRENT_SCHEMA()
        AND (
            TABLE_NAME LIKE 'TEST_%' OR 
            TABLE_NAME LIKE 'V_%' OR
            TABLE_NAME LIKE 'VIEW_%'
        );
    
    FOR record IN views_cursor DO
        EXECUTE IMMEDIATE 'DROP VIEW IF EXISTS ' || record.TABLE_NAME || ' CASCADE';
    END FOR;
END;

-- Drop all test sequences
BEGIN
    LET seq_cursor CURSOR FOR
        SELECT SEQUENCE_NAME 
        FROM INFORMATION_SCHEMA.SEQUENCES 
        WHERE SEQUENCE_SCHEMA = CURRENT_SCHEMA()
        AND (
            SEQUENCE_NAME LIKE 'TEST_%' OR 
            SEQUENCE_NAME LIKE 'SEQ_%'
        );
    
    FOR record IN seq_cursor DO
        EXECUTE IMMEDIATE 'DROP SEQUENCE IF EXISTS ' || record.SEQUENCE_NAME;
    END FOR;
END;

-- Drop all test procedures
BEGIN
    LET proc_cursor CURSOR FOR
        SELECT PROCEDURE_NAME 
        FROM INFORMATION_SCHEMA.PROCEDURES 
        WHERE PROCEDURE_SCHEMA = CURRENT_SCHEMA()
        AND PROCEDURE_NAME LIKE 'TEST_%';
    
    FOR record IN proc_cursor DO
        EXECUTE IMMEDIATE 'DROP PROCEDURE IF EXISTS ' || record.PROCEDURE_NAME || '()';
    END FOR;
END;

-- Drop all test functions
BEGIN
    LET func_cursor CURSOR FOR
        SELECT FUNCTION_NAME 
        FROM INFORMATION_SCHEMA.FUNCTIONS 
        WHERE FUNCTION_SCHEMA = CURRENT_SCHEMA()
        AND FUNCTION_NAME LIKE 'TEST_%';
    
    FOR record IN func_cursor DO
        EXECUTE IMMEDIATE 'DROP FUNCTION IF EXISTS ' || record.FUNCTION_NAME || '()';
    END FOR;
END;