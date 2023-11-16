CREATE TABLE test_table (test_column INT NULL);
ALTER TABLE test_table ADD varcharColumn VARCHAR(25) NULL, intColumn INT NULL, dateColumn date NULL;
UPDATE test_table SET varcharColumn = 'INITIAL_VALUE';
UPDATE test_table SET intColumn = 5;
UPDATE test_table SET dateColumn = '2020-09-21';