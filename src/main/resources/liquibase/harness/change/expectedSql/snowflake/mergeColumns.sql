CREATE TABLE ${CATALOG_NAME}.PUBLIC.full_name_table (first_name VARCHAR(50), last_name VARCHAR(50))
INSERT INTO ${CATALOG_NAME}.PUBLIC.full_name_table (first_name) VALUES ('John')
UPDATE ${CATALOG_NAME}.PUBLIC.full_name_table SET last_name = 'Doe' WHERE first_name='John'
INSERT INTO ${CATALOG_NAME}.PUBLIC.full_name_table (first_name) VALUES ('Jane')
UPDATE ${CATALOG_NAME}.PUBLIC.full_name_table SET last_name = 'Doe' WHERE first_name='Jane'
ALTER TABLE ${CATALOG_NAME}.PUBLIC.full_name_table ADD full_name VARCHAR(255)
UPDATE ${CATALOG_NAME}.PUBLIC.full_name_table SET full_name = first_name || ' ' || last_name
ALTER TABLE ${CATALOG_NAME}.PUBLIC.full_name_table DROP COLUMN first_name
ALTER TABLE ${CATALOG_NAME}.PUBLIC.full_name_table DROP COLUMN last_name