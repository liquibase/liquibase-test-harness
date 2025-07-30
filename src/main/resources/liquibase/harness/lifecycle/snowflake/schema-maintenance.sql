-- Snowflake Schema Maintenance Script
-- Run this periodically to clean up any orphaned test schemas

-- Show current test schemas and their age
SELECT 
    SCHEMA_NAME,
    CREATED,
    DATEDIFF('minute', CREATED, CURRENT_TIMESTAMP()) as AGE_MINUTES,
    CASE 
        WHEN DATEDIFF('minute', CREATED, CURRENT_TIMESTAMP()) > 60 THEN 'STALE'
        ELSE 'ACTIVE'
    END as STATUS
FROM INFORMATION_SCHEMA.SCHEMATA
WHERE SCHEMA_NAME LIKE 'TEST_%'
ORDER BY CREATED DESC;

-- Show current schema locks
SELECT * FROM TESTHARNESS.SCHEMA_LOCKS
ORDER BY LOCKED_AT DESC;

-- Clean up orphaned schemas (older than 1 hour)
BEGIN
    LET schemas_to_drop CURSOR FOR
        SELECT SCHEMA_NAME 
        FROM INFORMATION_SCHEMA.SCHEMATA
        WHERE SCHEMA_NAME LIKE 'TEST_%_%'  -- Pattern: TEST_<name>_<slot>
        AND DATEDIFF('minute', CREATED, CURRENT_TIMESTAMP()) > 60;
    
    LET dropped_count INTEGER := 0;
    
    FOR record IN schemas_to_drop DO
        BEGIN
            EXECUTE IMMEDIATE 'DROP SCHEMA IF EXISTS ' || record.SCHEMA_NAME || ' CASCADE';
            LET dropped_count := dropped_count + 1;
            
            -- Also remove from locks table if exists
            DELETE FROM TESTHARNESS.SCHEMA_LOCKS 
            WHERE SCHEMA_NAME = record.SCHEMA_NAME;
            
        EXCEPTION
            WHEN OTHER THEN
                -- Log but continue with other schemas
                SYSTEM$LOG('warning', 'Failed to drop schema: ' || record.SCHEMA_NAME);
        END;
    END FOR;
    
    RETURN 'Dropped ' || dropped_count || ' orphaned schemas';
END;

-- Clean up stale locks (older than 30 minutes)
DELETE FROM TESTHARNESS.SCHEMA_LOCKS 
WHERE DATEDIFF('minute', LOCKED_AT, CURRENT_TIMESTAMP()) > 30;

-- Emergency cleanup - drop ALL test schemas
-- UNCOMMENT ONLY IF NEEDED!
-- BEGIN
--     FOR record IN (SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME LIKE 'TEST_%') DO
--         EXECUTE IMMEDIATE 'DROP SCHEMA IF EXISTS ' || record.SCHEMA_NAME || ' CASCADE';
--     END FOR;
--     TRUNCATE TABLE TESTHARNESS.SCHEMA_LOCKS;
-- END;