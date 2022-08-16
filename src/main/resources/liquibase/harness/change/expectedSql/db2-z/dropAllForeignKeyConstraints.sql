INVALID TEST

Bug: DROP statement doesnt get generated

--ALTER TABLE "IBMUSER".posts ADD CONSTRAINT fk_posts_authors_test_1 FOREIGN KEY (author_id) REFERENCES "IBMUSER".authors (id)
--ALTER TABLE "IBMUSER".posts ADD CONSTRAINT fk_posts_authors_test_2 FOREIGN KEY (id) REFERENCES "IBMUSER".authors (id)