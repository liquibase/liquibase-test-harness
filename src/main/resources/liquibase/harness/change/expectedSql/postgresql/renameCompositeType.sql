CREATE TYPE public."myRenameType" AS (attr1 int, attr2 text COLLATE "en_US.utf8")
ALTER TYPE public."myRenameType" RENAME TO "myNewNameType"