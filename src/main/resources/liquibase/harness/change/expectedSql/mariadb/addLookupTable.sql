CREATE TABLE posts_details AS SELECT DISTINCT inserted_date AS post_date FROM posts WHERE inserted_date IS NOT NULL
ALTER TABLE posts_details MODIFY post_date date NOT NULL
ALTER TABLE posts_details ADD PRIMARY KEY (post_date)
ALTER TABLE posts ADD CONSTRAINT FK_POSTS_POSTS_DETAILS FOREIGN KEY (inserted_date) REFERENCES posts_details (post_date)