ALTER TABLE authors ADD CONSTRAINT test_unique_constraint UNIQUE (email)
ALTER TABLE authors DROP KEY test_unique_constraint