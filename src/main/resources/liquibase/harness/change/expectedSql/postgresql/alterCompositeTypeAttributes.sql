DROP TYPE IF EXISTS public."myType" CASCADE
CREATE TYPE public."myType" AS (attr0 int, attr1 int, attr2 int)
ALTER TYPE public."myType" ADD ATTRIBUTE attr3 text COLLATE "en-US-x-icu" CASCADE, ALTER ATTRIBUTE attr0 SET DATA TYPE text COLLATE "en-US-x-icu" RESTRICT, DROP ATTRIBUTE IF EXISTS attr1 CASCADE