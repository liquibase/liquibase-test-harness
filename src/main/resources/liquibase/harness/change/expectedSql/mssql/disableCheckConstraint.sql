ALTER TABLE posts ADD CONSTRAINT test_check_constraint CHECK (id > 0)
ALTER TABLE posts NOCHECK CONSTRAINT test_check_constraint