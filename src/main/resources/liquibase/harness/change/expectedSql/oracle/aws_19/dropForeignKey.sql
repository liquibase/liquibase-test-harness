ALTER TABLE LBUSER.posts ADD CONSTRAINT fk_posts_authors_test FOREIGN KEY (author_id) REFERENCES LBUSER.authors (id) ON DELETE CASCADE
ALTER TABLE LBUSER.posts DROP CONSTRAINT fk_posts_authors_test