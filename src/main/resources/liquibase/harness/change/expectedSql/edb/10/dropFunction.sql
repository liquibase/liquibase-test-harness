ALTER SESSION SET SEARCH_PATH=public
CREATE OR REPLACE FUNCTION public.test_function()
RETURNS trigger
AS $$
BEGIN
RAISE NOTICE 'Test function created'
RETURN NEW
END
$$
LANGUAGE plpgsql
DROP FUNCTION public.test_function()