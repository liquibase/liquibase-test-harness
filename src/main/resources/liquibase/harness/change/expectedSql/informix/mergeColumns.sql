CREATE TABLE testdb:informix.full_name_table (first_name VARCHAR(50), last_name VARCHAR(50))
INSERT INTO testdb:informix.full_name_table (first_name) VALUES ('John')
UPDATE testdb:informix.full_name_table SET last_name = 'Doe' WHERE first_name='John'
INSERT INTO testdb:informix.full_name_table (first_name) VALUES ('Jane')
UPDATE testdb:informix.full_name_table SET last_name = 'Doe' WHERE first_name='Jane'
ALTER TABLE testdb:informix.full_name_table ADD full_name VARCHAR(255)
UPDATE testdb:informix.full_name_table SET full_name = first_name || ' ' || last_name
ALTER TABLE testdb:informix.full_name_table DROP first_name
ALTER TABLE testdb:informix.full_name_table DROP last_name