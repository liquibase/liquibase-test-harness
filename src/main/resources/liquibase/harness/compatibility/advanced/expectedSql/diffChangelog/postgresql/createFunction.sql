CREATE OR REPLACE FUNCTION "public".test_function()
RETURNS character varying
LANGUAGE sql
RETURN 'Hello'::text
DROP FUNCTION "public"."secondary_function"