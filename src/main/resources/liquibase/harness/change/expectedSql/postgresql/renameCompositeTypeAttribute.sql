CREATE TYPE public."myType" AS (attr1 int, attr2 text COLLATE "en_US")
ALTER TYPE public."myType" RENAME ATTRIBUTE attr2 TO attr22 CASCADE