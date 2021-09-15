ALTER TABLE lbcat.authors ADD CONSTRAINT test_unique_constraint UNIQUE (email)
ALTER TABLE lbcat.authors DROP KEY test_unique_constraint