ALTER TABLE "public".posts ADD CONSTRAINT fk_posts_authors_test FOREIGN KEY (author_id) REFERENCES "public".authors (id)
ALTER TABLE "public".posts DROP CONSTRAINT fk_posts_authors_test