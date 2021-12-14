ALTER TABLE lbcat.posts ADD CONSTRAINT test_check_constraint CHECK (author_id > 0)
ALTER TABLE lbcat.posts DROP CONSTRAINT test_check_constraint