CREATE OR REPLACE FUNCTION test_function()
RETURNS trigger
SET SCHEMA 'public'
AS $$
BEGIN
RAISE NOTICE 'Canned Spam in a frying pan: ick'
RETURN NEW
END
$$
LANGUAGE plpgsql
CREATE TRIGGER test_trigger
BEFORE INSERT ON public.posts
FOR EACH ROW EXECUTE PROCEDURE test_function()