INVALID TEST

Bug: DROP statement doesnt get generated

--ALTER TABLE "LTHUSER".posts ADD CONSTRAINT fk_posts_authors_test_1 FOREIGN KEY (author_id) REFERENCES "LTHUSER".authors (id)
--ALTER TABLE "LTHUSER".posts ADD CONSTRAINT fk_posts_authors_test_2 FOREIGN KEY (id) REFERENCES "LTHUSER".authors (id)