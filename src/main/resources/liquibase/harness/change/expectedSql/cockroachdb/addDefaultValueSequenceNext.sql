CREATE SEQUENCE  IF NOT EXISTS public.test_sequence INCREMENT BY 1 MINVALUE 1
ALTER TABLE public.authors ADD sequence_referenced_column INTEGER
ALTER TABLE public.authors ALTER COLUMN  sequence_referenced_column SET DEFAULT nextval('public.test_sequence')
ALTER SEQUENCE public.test_sequence OWNED BY public.authors.sequence_referenced_column