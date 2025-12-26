ALTER TABLE ${CATALOG_NAME}.PUBLIC.authors ADD CONSTRAINT test_unique_constraint UNIQUE (email)
ALTER TABLE ${CATALOG_NAME}.PUBLIC.authors DROP CONSTRAINT test_unique_constraint