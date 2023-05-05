ALTER TABLE ADMIN.posts ADD CONSTRAINT fk_posts_authors_test FOREIGN KEY (author_id) REFERENCES ADMIN.authors (id) ON DELETE CASCADE
ALTER TABLE ADMIN.posts DROP CONSTRAINT fk_posts_authors_test