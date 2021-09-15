ALTER TABLE public.authors ADD CONSTRAINT test_unique_constraint UNIQUE (email)
DROP INDEX test_unique_constraint CASCADE