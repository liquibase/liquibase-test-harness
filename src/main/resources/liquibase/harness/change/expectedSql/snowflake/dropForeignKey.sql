ALTER TABLE "PUBLIC".posts ADD CONSTRAINT fk_posts_authors_test FOREIGN KEY (author_id) REFERENCES "PUBLIC".authors (id)
ALTER TABLE "PUBLIC".posts DROP CONSTRAINT fk_posts_authors_test