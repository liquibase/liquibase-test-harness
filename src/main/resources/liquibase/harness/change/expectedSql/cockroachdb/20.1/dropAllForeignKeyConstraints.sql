CREATE INDEX "authors.id.index" ON authors(id)
CREATE INDEX "posts.id.index" ON posts(id)
CREATE INDEX "posts.author_id.index" ON posts(author_id)
ALTER TABLE posts ADD CONSTRAINT fk_posts_authors_test_1 FOREIGN KEY (author_id) REFERENCES authors (id)
ALTER TABLE posts ADD CONSTRAINT fk_posts_authors_test_2 FOREIGN KEY (id) REFERENCES authors (id)
DROP INDEX "authors.id.index"
DROP INDEX "posts.id.index"
DROP INDEX "posts.author_id.index"