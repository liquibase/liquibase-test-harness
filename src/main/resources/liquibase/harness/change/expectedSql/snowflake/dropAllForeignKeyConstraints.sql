<<<<<<< HEAD
ALTER TABLE "public".posts ADD CONSTRAINT fk_posts_authors_test_1 FOREIGN KEY (author_id) REFERENCES "public".authors (id)
ALTER TABLE "public".posts ADD CONSTRAINT fk_posts_authors_test_2 FOREIGN KEY (id) REFERENCES "public".authors (id)
=======
ALTER TABLE LTHDB.PUBLIC.posts ADD CONSTRAINT fk_posts_authors_test_1 FOREIGN KEY (author_id) REFERENCES LTHDB.PUBLIC.authors (id)
ALTER TABLE LTHDB.PUBLIC.posts ADD CONSTRAINT fk_posts_authors_test_2 FOREIGN KEY (id) REFERENCES LTHDB.PUBLIC.authors (id)
>>>>>>> main
