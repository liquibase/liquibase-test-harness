ALTER TABLE public.authors ADD CONSTRAINT test_unique_constraint UNIQUE (email) DEFERRABLE INITIALLY DEFERRED
ALTER TABLE public.authors DROP CONSTRAINT test_unique_constraint