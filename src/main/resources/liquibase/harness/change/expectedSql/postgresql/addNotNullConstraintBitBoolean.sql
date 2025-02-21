CREATE TABLE public.test_table (id INTEGER, bit_col BOOLEAN, boolean_col BOOLEAN)
    INSERT INTO public.test_table (id) VALUES (1)
UPDATE public.test_table SET bit_col = B'1' WHERE bit_col IS NULL
ALTER TABLE public.test_table ALTER COLUMN  bit_col SET NOT NULL
UPDATE public.test_table SET boolean_col = 'TRUE' WHERE boolean_col IS NULL
ALTER TABLE public.test_table ALTER COLUMN  boolean_col SET NOT NULL