CREATE TABLE IF NOT EXISTS public."camelCase" (id SERIAL NOT NULL, CONSTRAINT "camelCase_pkey" PRIMARY KEY (id))
DO $$ DECLARE constraint_name varchar
BEGIN
SELECT tc.CONSTRAINT_NAME into strict constraint_name
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
WHERE CONSTRAINT_TYPE = 'PRIMARY KEY'
AND TABLE_NAME = 'camelCase' AND TABLE_SCHEMA = 'public'
EXECUTE 'alter table public."camelCase" drop constraint "' || constraint_name || '"'
END $$