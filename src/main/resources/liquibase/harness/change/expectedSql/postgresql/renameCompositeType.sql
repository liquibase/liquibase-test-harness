DROP TYPE IF EXISTS public."myType" CASCADE
CREATE TYPE public."myRenameType" AS (attr1 int, attr2 text COLLATE "en-US-x-icu")
ALTER TYPE public."myRenameType" RENAME TO "myNewNameType"