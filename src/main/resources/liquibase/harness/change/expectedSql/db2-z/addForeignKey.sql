ALTER TABLE "IBMUSER".posts ADD CONSTRAINT fk_posts_authors_test FOREIGN KEY (author_id) REFERENCES "IBMUSER".authors (id) ON DELETE CASCADE