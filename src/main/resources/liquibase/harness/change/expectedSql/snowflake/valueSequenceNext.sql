CREATE TABLE ${CATALOG_NAME}.PUBLIC.valueSequenceNextTable (test_id INT, test_column VARCHAR(50));
CREATE SEQUENCE ${CATALOG_NAME}.PUBLIC.test_sequence START WITH 30 INCREMENT BY 2;
UPDATE ${CATALOG_NAME}.PUBLIC.valueSequenceNextTable SET test_id = PUBLIC.test_sequence.nextval;