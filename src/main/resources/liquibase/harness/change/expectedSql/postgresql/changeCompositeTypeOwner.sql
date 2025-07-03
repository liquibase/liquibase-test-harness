DROP TYPE IF EXISTS public."myType" CASCADE
CREATE TYPE public."myType" AS (attr1 int, attr2 text COLLATE "en-US-x-icu")
ALTER TYPE public."myType" OWNER TO SESSION_USER