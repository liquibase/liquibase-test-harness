CREATE TABLE LTHDB.TESTHARNESS.full_name_table (first_name VARCHAR(50), last_name VARCHAR(50))
INSERT INTO LTHDB.TESTHARNESS.full_name_table (first_name) VALUES ('John')
UPDATE LTHDB.TESTHARNESS.full_name_table SET last_name = 'Doe' WHERE first_name='John'
INSERT INTO LTHDB.TESTHARNESS.full_name_table (first_name) VALUES ('Jane')
UPDATE LTHDB.TESTHARNESS.full_name_table SET last_name = 'Doe' WHERE first_name='Jane'
ALTER TABLE LTHDB.TESTHARNESS.full_name_table ADD full_name VARCHAR(255)
UPDATE LTHDB.TESTHARNESS.full_name_table SET full_name = first_name || ' ' || last_name
ALTER TABLE LTHDB.TESTHARNESS.full_name_table DROP COLUMN first_name
ALTER TABLE LTHDB.TESTHARNESS.full_name_table DROP COLUMN last_name