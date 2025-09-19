-- Snowflake Test Harness Initialization Script
-- This script runs before each test to ensure clean state

-- Ensure we have an active warehouse for warehouse operations
USE WAREHOUSE LTHDB_TEST_WH;