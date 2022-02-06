ALTER TABLE LBUSER.posts ADD CONSTRAINT test_check_constraint CHECK (id > 0)
ALTER TABLE LBUSER.posts DROP CONSTRAINT test_check_constraint