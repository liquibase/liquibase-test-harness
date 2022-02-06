ALTER TABLE public.posts ADD CONSTRAINT test_check_constraint CHECK (id > 0)
ALTER TABLE public.posts DROP CONSTRAINT test_check_constraint