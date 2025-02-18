CREATE TABLE test_table (test_column INT NULL, varcharColumn VARCHAR(25) NULL, intColumn INT NULL, dateColumn date NULL);

CREATE FUNCTION `test_function`() RETURNS varchar(20) CHARSET latin1
BEGIN
                                              RETURN \'Hello\';
                                              END;