CREATE INDEX author_id_idx ON public.posts(author_id)
ALTER TABLE public.posts ADD CONSTRAINT fk_posts_authors_test FOREIGN KEY (author_id) REFERENCES public.authors (id) ON UPDATE RESTRICT ON DELETE CASCADE
ALTER TABLE public.posts DROP CONSTRAINT fk_posts_authors_test
DROP INDEX public.author_id_idx
