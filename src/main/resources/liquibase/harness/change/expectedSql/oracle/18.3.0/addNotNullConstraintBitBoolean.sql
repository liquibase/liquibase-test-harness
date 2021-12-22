CREATE TABLE DATICAL_ADMIN.test_table (id INTEGER, bit_col NUMBER(1), boolean_col NUMBER(1))
INSERT INTO DATICAL_ADMIN.test_table (id) VALUES (1)
UPDATE DATICAL_ADMIN.test_table SET bit_col = 1 WHERE bit_col IS NULL
ALTER TABLE DATICAL_ADMIN.test_table MODIFY bit_col NOT NULL
UPDATE DATICAL_ADMIN.test_table SET boolean_col = 1 WHERE boolean_col IS NULL
ALTER TABLE DATICAL_ADMIN.test_table MODIFY boolean_col NOT NULL