CREATE OR REPLACE FUNCTION test_function()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
            BEGIN
            RAISE NOTICE 'Test trigger function created';
            RETURN NEW;
            END;
            $function$;

CREATE TRIGGER test_trigger BEFORE INSERT ON posts FOR EACH ROW EXECUTE PROCEDURE test_function();