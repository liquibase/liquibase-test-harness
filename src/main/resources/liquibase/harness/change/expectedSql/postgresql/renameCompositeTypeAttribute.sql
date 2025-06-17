CREATE TYPE public."myType" AS (attr1 int, attr2 text COLLATE "en-US-x-icu", "Attr3" real, ATTR4 line)
ALTER TYPE public."myType" RENAME ATTRIBUTE attr1 TO "att®1" CASCADE
ALTER TYPE public."myType" RENAME ATTRIBUTE attr2 TO "attribute 2" CASCADE
ALTER TYPE public."myType" RENAME ATTRIBUTE "Attr3" TO "AtTrIbUtE3" CASCADE
ALTER TYPE public."myType" RENAME ATTRIBUTE ATTR4 TO "attr & 4" CASCADE