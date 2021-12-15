CREATE TABLE lbcat.full_name_table (id INT AUTO_INCREMENT NOT NULL, first_name VARCHAR(50) NULL, last_name VARCHAR(50) NULL, CONSTRAINT PK_FULL_NAME_TABLE PRIMARY KEY (id))
INSERT INTO lbcat.full_name_table (first_name) VALUES ('John')
UPDATE lbcat.full_name_table SET last_name = 'Doe' WHERE first_name='John'
INSERT INTO lbcat.full_name_table (first_name) VALUES ('Jane')
UPDATE lbcat.full_name_table SET last_name = 'Doe' WHERE first_name='Jane'
ALTER TABLE lbcat.full_name_table ADD full_name VARCHAR(255) NULL
UPDATE lbcat.full_name_table SET full_name = CONCAT_WS(' ', first_name, last_name)
ALTER TABLE lbcat.full_name_table DROP COLUMN first_name
ALTER TABLE lbcat.full_name_table DROP COLUMN last_name

