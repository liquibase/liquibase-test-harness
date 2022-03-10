ALTER TABLE LBUSER.posts ADD CONSTRAINT test_check_constraint CHECK (id > 0) DISABLE
ALTER TABLE LBUSER.posts ENABLE CONSTRAINT test_check_constraint