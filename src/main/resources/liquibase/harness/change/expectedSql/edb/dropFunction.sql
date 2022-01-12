NEW\nEND\n$$\nLANGUAGE plpgsql\nDROP FUNCTION public.test_function
CREATE OR REPLACE FUNCTION public.test_function()
RETURNS trigger
AS $$
BEGIN
RAISE NOTICE 'Test function created'
RETURN NEW
END
$$
LANGUAGE plpgsql
DROP FUNCTION public.test_function