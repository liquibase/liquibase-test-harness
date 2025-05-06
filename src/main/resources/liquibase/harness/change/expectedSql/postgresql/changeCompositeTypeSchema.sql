CREATE TYPE public."myTypeSchema" AS (attr1 integer, attr2 text COLLATE "en_US");
CREATE SCHEMA test_schema;
ALTER TYPE public."myTypeSchema" SET SCHEMA test_schema;