CREATE TABLE public.autoincrement_test ("intColumn" INTEGER NOT NULL, "dateColumn" date, CONSTRAINT autoincrement_test_pkey PRIMARY KEY ("intColumn"))
CREATE SEQUENCE  IF NOT EXISTS public."autoincrement_test_intColumn_seq" START WITH 100 INCREMENT BY 2
ALTER TABLE public.autoincrement_test ALTER COLUMN  "intColumn" SET NOT NULL
ALTER TABLE public.autoincrement_test ALTER COLUMN  "intColumn" SET DEFAULT nextval('public."autoincrement_test_intColumn_seq"')
ALTER SEQUENCE public."autoincrement_test_intColumn_seq" OWNED BY public.autoincrement_test."intColumn"