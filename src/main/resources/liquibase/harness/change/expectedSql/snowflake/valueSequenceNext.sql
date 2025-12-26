CREATE TABLE ${CATALOG_NAME}.${SCHEMA_NAME}.valueSequenceNextTable (test_id INT, test_column VARCHAR(50));
CREATE SEQUENCE PUBLIC.test_sequence START WITH 30 INCREMENT BY 2;
UPDATE ${CATALOG_NAME}.${SCHEMA_NAME}.valueSequenceNextTable SET test_id = PUBLIC.test_sequence.nextval;