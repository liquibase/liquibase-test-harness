ALTER TABLE LTHDB.PUBLIC.authors ADD CONSTRAINT test_unique_constraint UNIQUE (email)
ALTER TABLE LTHDB.PUBLIC.authors DROP CONSTRAINT test_unique_constraint