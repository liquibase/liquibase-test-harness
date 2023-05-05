ALTER TABLE ADMIN.posts ADD CONSTRAINT test_check_constraint CHECK (id > 0) DISABLE
ALTER TABLE ADMIN.posts ENABLE CONSTRAINT test_check_constraint