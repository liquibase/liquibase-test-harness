ALTER TABLE APP.authors ADD CONSTRAINT test_unique_constraint UNIQUE (email)
ALTER TABLE APP.authors DROP CONSTRAINT test_unique_constraint