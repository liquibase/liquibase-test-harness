CREATE TABLE lbcat.test_table (id INT NULL, bit_col BIT(1) NULL, boolean_col BIT(1) NULL)
INSERT INTO lbcat.test_table (id) VALUES (1)
UPDATE lbcat.test_table SET bit_col = 1 WHERE bit_col IS NULL
ALTER TABLE lbcat.test_table MODIFY bit_col BIT(1) NOT NULL
UPDATE lbcat.test_table SET boolean_col = 1 WHERE boolean_col IS NULL
ALTER TABLE lbcat.test_table MODIFY boolean_col BIT(1) NOT NULL