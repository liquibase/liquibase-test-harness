ALTER TABLE testdb:informix.authors ADD CONSTRAINT UNIQUE (email) CONSTRAINT test_unique_constraint
ALTER TABLE testdb:informix.authors DROP CONSTRAINT test_unique_constraint