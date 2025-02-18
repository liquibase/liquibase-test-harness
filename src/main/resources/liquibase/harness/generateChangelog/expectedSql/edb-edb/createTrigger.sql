CREATE OR REPLACE FUNCTION "public".test_function()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
                                                        BEGIN
                                                            RAISE NOTICE \'Test trigger function created\';
                                                            RETURN NEW;
                                                        END;
                                                    $function$;

CREATE TRIGGER test_trigger BEFORE INSERT ON "public".posts FOR EACH ROW EXECUTE FUNCTION test_function();