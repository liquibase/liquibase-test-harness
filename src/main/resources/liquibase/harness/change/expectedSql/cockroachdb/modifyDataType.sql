SET enable_experimental_alter_column_type_general = true
CREATE TABLE public.modify_data_type_test ("intColumn" INTEGER, "dateColumn" date)
ALTER TABLE public.modify_data_type_test ALTER COLUMN "intColumn" TYPE VARCHAR(50) USING ("intColumn"::VARCHAR(50))