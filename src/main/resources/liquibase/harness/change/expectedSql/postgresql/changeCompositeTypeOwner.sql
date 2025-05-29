CREATE TYPE public."myType" AS (attr1 int, attr2 text COLLATE "en_US.utf8")
ALTER TYPE public."myType" OWNER TO SESSION_USER