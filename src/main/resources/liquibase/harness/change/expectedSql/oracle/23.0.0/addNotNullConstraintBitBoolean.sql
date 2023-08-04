CREATE TABLE LIQUIBASE.test_table (id INTEGER, bit_col NUMBER(1), boolean_col NUMBER(1))
INSERT INTO LIQUIBASE.test_table (id) VALUES (1)
UPDATE LIQUIBASE.test_table SET bit_col = 1 WHERE bit_col IS NULL
ALTER TABLE LIQUIBASE.test_table MODIFY bit_col NOT NULL
UPDATE LIQUIBASE.test_table SET boolean_col = 1 WHERE boolean_col IS NULL
ALTER TABLE LIQUIBASE.test_table MODIFY boolean_col NOT NULL