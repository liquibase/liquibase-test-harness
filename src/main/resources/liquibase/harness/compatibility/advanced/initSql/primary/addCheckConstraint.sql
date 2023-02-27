CREATE TABLE lbcat.test_table_xml (test_column INT NULL)
ALTER TABLE lbcat.test_table_xml ADD CONSTRAINT test_check_constraint CHECK (test_column > 0)