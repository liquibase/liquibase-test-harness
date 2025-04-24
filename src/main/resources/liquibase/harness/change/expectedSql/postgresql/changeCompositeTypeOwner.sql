CREATE TYPE public."myType" AS (attr1 int, attr2 text COLLATE "en_US")
ALTER TYPE public."myType" OWNER TO SESSION_USER