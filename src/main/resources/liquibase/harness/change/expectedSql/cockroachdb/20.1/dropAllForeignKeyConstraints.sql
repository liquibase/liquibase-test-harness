CREATE INDEX "authors.id.index" ON public.authors(id)
CREATE INDEX "posts.id.index" ON public.posts(id)
CREATE INDEX "posts.author_id.index" ON public.posts(author_id)
ALTER TABLE public.posts ADD CONSTRAINT fk_posts_authors_test_1 FOREIGN KEY (author_id) REFERENCES public.authors (id)
ALTER TABLE public.posts ADD CONSTRAINT fk_posts_authors_test_2 FOREIGN KEY (id) REFERENCES public.authors (id)
DROP INDEX public."authors.id.index"
DROP INDEX public."posts.id.index"
DROP INDEX public."posts.author_id.index"