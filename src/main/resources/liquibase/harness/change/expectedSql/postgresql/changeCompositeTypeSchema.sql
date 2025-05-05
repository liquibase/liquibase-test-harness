CREATE TYPE public."myType" AS (attr1 integer, attr2 text COLLATE "en_US");
CREATE SCHEMA IF NOT EXISTS test_schema;
ALTER TYPE public."myType" SET SCHEMA test_schema;
SELECT 1 FROM pg_catalog.pg_type t 
JOIN pg_catalog.pg_namespace n ON t.typnamespace = n.oid
WHERE t.typname = 'myType' AND n.nspname = 'test_schema';