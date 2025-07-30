-- Snowflake Test Harness Initialization Script
-- This script runs before each test to ensure clean state

-- Ensure DATABASECHANGELOGLOCK is unlocked
UPDATE DATABASECHANGELOGLOCK SET LOCKED = FALSE WHERE ID = 1;

-- Clean any previous test runs (except init.xml)
DELETE FROM DATABASECHANGELOG WHERE FILENAME NOT LIKE '%init.xml';