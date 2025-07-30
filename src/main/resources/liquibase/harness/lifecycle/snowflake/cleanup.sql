-- Snowflake Test Harness Cleanup Script
-- This script runs after each test to clean up persistent state

-- Clear DATABASECHANGELOG entries (except for init.xml)
DELETE FROM DATABASECHANGELOG WHERE FILENAME NOT LIKE '%init.xml';

-- Ensure DATABASECHANGELOGLOCK is released
UPDATE DATABASECHANGELOGLOCK SET LOCKED = FALSE WHERE ID = 1;

-- Drop common test tables (add patterns as needed)
DROP TABLE IF EXISTS authors CASCADE;
DROP TABLE IF EXISTS posts CASCADE;
DROP TABLE IF EXISTS books CASCADE;
DROP TABLE IF EXISTS test_table CASCADE;
DROP TABLE IF EXISTS example_table CASCADE;
DROP VIEW IF EXISTS authors_view CASCADE;
DROP SEQUENCE IF EXISTS test_seq;
DROP PROCEDURE IF EXISTS test_proc();
DROP FUNCTION IF EXISTS test_func();