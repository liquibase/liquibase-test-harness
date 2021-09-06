CREATE TABLE full_name_table (first_name VARCHAR(50), last_name VARCHAR(50))
INSERT INTO full_name_table (first_name) VALUES ('John')
UPDATE full_name_table SET last_name = 'Doe' WHERE first_name='John'
INSERT INTO full_name_table (first_name) VALUES ('Jane')
UPDATE full_name_table SET last_name = 'Doe' WHERE first_name='Jane'
ALTER TABLE full_name_table ADD full_name VARCHAR(255)
UPDATE full_name_table SET full_name = CONCAT(first_name, CONCAT(' ', last_name))
ALTER TABLE full_name_table DROP COLUMN first_name
ALTER TABLE full_name_table DROP COLUMN last_name