CREATE OR REPLACE FUNCTION public.test_function()
RETURNS trigger
AS $$
BEGIN
RAISE NOTICE 'Test function created'
RETURN NEW
END
$$
LANGUAGE plpgsql