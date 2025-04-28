CREATE TYPE public."myRenameType" AS (attr1 int, attr2 text COLLATE "en_US")
ALTER TYPE public."myRenameType" RENAME TO "myNewNameType"