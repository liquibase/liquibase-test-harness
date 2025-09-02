-- Check if DATABASECHANGELOGLOCK table exists and its contents
SHOW TABLES LIKE 'DATABASECHANGELOGLOCK';

-- If it exists, show its contents
SELECT * FROM DATABASECHANGELOGLOCK;

-- Show current schema and database context
SELECT CURRENT_DATABASE(), CURRENT_SCHEMA(), CURRENT_ROLE(), CURRENT_WAREHOUSE();