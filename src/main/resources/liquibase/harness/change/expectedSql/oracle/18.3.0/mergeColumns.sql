CREATE TABLE DATICAL_ADMIN.full_name_table (first_name VARCHAR2(50), last_name VARCHAR2(50))
INSERT INTO DATICAL_ADMIN.full_name_table (first_name) VALUES ('John')
UPDATE DATICAL_ADMIN.full_name_table SET last_name = 'Doe' WHERE first_name='John'
INSERT INTO DATICAL_ADMIN.full_name_table (first_name) VALUES ('Jane')
UPDATE DATICAL_ADMIN.full_name_table SET last_name = 'Doe' WHERE first_name='Jane'
ALTER TABLE DATICAL_ADMIN.full_name_table ADD full_name VARCHAR2(255)
UPDATE DATICAL_ADMIN.full_name_table SET full_name = first_name || ' ' || last_name
ALTER TABLE DATICAL_ADMIN.full_name_table DROP COLUMN first_name
ALTER TABLE DATICAL_ADMIN.full_name_table DROP COLUMN last_name