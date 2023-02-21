CREATE INDEX test_table_reference_index ON test_table_reference(test_column)
ALTER TABLE test_table_base ADD CONSTRAINT test_fk FOREIGN KEY (id) REFERENCES test_table_reference (test_column) ON UPDATE RESTRICT ON DELETE CASCADE
ALTER TABLE test_table_reference DROP FOREIGN KEY secondary_test_fk
DROP INDEX test_table_base_index ON test_table_base