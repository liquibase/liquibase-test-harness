ALTER TABLE ADMIN.posts ADD CONSTRAINT test_check_constraint CHECK (id > 0)
ALTER TABLE ADMIN.posts DROP CONSTRAINT test_check_constraint