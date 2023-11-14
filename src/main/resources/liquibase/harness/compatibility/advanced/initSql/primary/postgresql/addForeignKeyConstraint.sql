CREATE TABLE test_table_base(id INT NOT NULL, test_column INT NULL, PRIMARY KEY(id));
CREATE TABLE test_table_reference(id INT NOT NULL, test_column INT NULL UNIQUE, PRIMARY KEY(id));
ALTER TABLE test_table_base ADD CONSTRAINT test_fk FOREIGN KEY (id) REFERENCES test_table_reference (test_column);