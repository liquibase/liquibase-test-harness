CREATE TABLE lbcat.test_table (test_column INT NULL)
ALTER TABLE lbcat.test_table ADD varcharColumn VARCHAR(25) NULL, ADD intColumn INT NULL, ADD dateColumn date NULL
UPDATE lbcat.test_table SET varcharColumn = 'INITIAL_VALUE'
UPDATE lbcat.test_table SET intColumn = 5
UPDATE lbcat.test_table SET dateColumn = '2020-09-21'