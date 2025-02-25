CREATE TABLE test_table_base (id INT NOT NULL, PRIMARY KEY (id) CONSTRAINT u105_22);

CREATE TABLE test_table_reference (id INT NOT NULL, test_column INT NOT NULL, PRIMARY KEY (id) CONSTRAINT u106_24);

CREATE UNIQUE INDEX 106_27 ON test_table_reference(test_column);

ALTER TABLE test_table_base ADD CONSTRAINT  FOREIGN KEY (id) REFERENCES test_table_reference (test_column) ON DELETE CASCADE CONSTRAINT test_fk;