CREATE TABLE test_table_xml (test_column INT DEFAULT NULL NULL)
ALTER TABLE test_table_xml ADD CONSTRAINT test_check_constraint CHECK (`test_column` > 0)