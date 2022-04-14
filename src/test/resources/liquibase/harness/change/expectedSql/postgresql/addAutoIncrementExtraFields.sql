CREATE TABLE public.test_table (autoinc_test INTEGER)
CREATE SEQUENCE  IF NOT EXISTS public.test_table_autoinc_test_seq START WITH 10 INCREMENT BY 2
ALTER TABLE public.test_table ALTER COLUMN  autoinc_test SET NOT NULL
ALTER TABLE public.test_table ALTER COLUMN  autoinc_test SET DEFAULT nextval('public.test_table_autoinc_test_seq')
ALTER SEQUENCE public.test_table_autoinc_test_seq OWNED BY public.test_table.autoinc_test