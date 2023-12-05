CREATE TABLE "C##LIQUIBASE".test_table (id INTEGER, bit_col BOOLEAN, boolean_col BOOLEAN)
INSERT INTO "C##LIQUIBASE".test_table (id) VALUES (1)
UPDATE "C##LIQUIBASE".test_table SET bit_col = 1 WHERE bit_col IS NULL
ALTER TABLE "C##LIQUIBASE".test_table MODIFY bit_col NOT NULL
UPDATE "C##LIQUIBASE".test_table SET boolean_col = 1 WHERE boolean_col IS NULL
ALTER TABLE "C##LIQUIBASE".test_table MODIFY boolean_col NOT NULL