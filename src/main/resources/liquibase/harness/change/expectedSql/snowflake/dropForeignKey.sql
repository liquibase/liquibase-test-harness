<<<<<<< HEAD
ALTER TABLE "public".posts ADD CONSTRAINT fk_posts_authors_test FOREIGN KEY (author_id) REFERENCES "public".authors (id)
ALTER TABLE "public".posts DROP CONSTRAINT fk_posts_authors_test
=======
ALTER TABLE LTHDB.PUBLIC.posts ADD CONSTRAINT fk_posts_authors_test FOREIGN KEY (author_id) REFERENCES LTHDB.PUBLIC.authors (id)
ALTER TABLE LTHDB.PUBLIC.posts DROP CONSTRAINT fk_posts_authors_test
>>>>>>> main
