USE master
CREATE TABLE test_table (id int, bit_col bit, boolean_col bit)
INSERT INTO test_table (id) VALUES (1)
UPDATE test_table SET bit_col = 1 WHERE bit_col IS NULL
ALTER TABLE test_table ALTER COLUMN bit_col bit NOT NULL
UPDATE test_table SET boolean_col = 1 WHERE boolean_col IS NULL
ALTER TABLE test_table ALTER COLUMN boolean_col bit NOT NULL