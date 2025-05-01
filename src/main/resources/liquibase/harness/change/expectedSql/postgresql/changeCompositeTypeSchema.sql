CREATE TYPE public."myTestType" AS (attr1 int, attr2 text COLLATE "en_US");
CREATE SCHEMA IF NOT EXISTS test_schema;
ALTER TYPE public."myTestType" SET SCHEMA test_schema;