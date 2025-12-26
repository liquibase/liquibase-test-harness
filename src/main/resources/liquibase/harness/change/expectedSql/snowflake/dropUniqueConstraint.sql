ALTER TABLE ${CATALOG_NAME}.${SCHEMA_NAME}.authors ADD CONSTRAINT test_unique_constraint UNIQUE (email)
ALTER TABLE ${CATALOG_NAME}.${SCHEMA_NAME}.authors DROP CONSTRAINT test_unique_constraint