CREATE TYPE public."myType" AS (attr1 integer, attr2 text COLLATE "en_US");
CREATE SCHEMA IF NOT EXISTS test_schema;
ALTER TYPE public."myType" SET SCHEMA test_schema;