ALTER TABLE LIQUIBASE.posts ADD CONSTRAINT test_check_constraint CHECK (id > 0)
ALTER TABLE LIQUIBASE.posts DROP CONSTRAINT test_check_constraint