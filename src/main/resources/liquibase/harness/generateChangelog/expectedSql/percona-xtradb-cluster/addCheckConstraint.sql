ALTER TABLE posts ADD CONSTRAINT test_check_constraint CHECK ((`author_id` > 0));