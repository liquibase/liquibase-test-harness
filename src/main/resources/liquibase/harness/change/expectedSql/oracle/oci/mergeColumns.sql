CREATE TABLE ADMIN.full_name_table (first_name VARCHAR2(50), last_name VARCHAR2(50))
INSERT INTO ADMIN.full_name_table (first_name) VALUES ('John')
UPDATE ADMIN.full_name_table SET last_name = 'Doe' WHERE first_name='John'
INSERT INTO ADMIN.full_name_table (first_name) VALUES ('Jane')
UPDATE ADMIN.full_name_table SET last_name = 'Doe' WHERE first_name='Jane'
ALTER TABLE ADMIN.full_name_table ADD full_name VARCHAR2(255)
UPDATE ADMIN.full_name_table SET full_name = first_name || ' ' || last_name
ALTER TABLE ADMIN.full_name_table DROP COLUMN first_name
ALTER TABLE ADMIN.full_name_table DROP COLUMN last_name