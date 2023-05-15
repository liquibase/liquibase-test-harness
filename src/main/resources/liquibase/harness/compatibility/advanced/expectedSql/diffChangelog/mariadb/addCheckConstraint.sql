ALTER TABLE test_table_xml ADD CONSTRAINT test_check_constraint CHECK (`test_column` > 0)
ALTER TABLE test_table_xml DROP CONSTRAINT secondary_check_constraint