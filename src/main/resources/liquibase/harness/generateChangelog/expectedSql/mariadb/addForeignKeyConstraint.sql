CREATE TABLE lbcat.test_table_base (id INT NOT NULL, CONSTRAINT PK_TEST_TABLE_BASE PRIMARY KEY (id));

CREATE TABLE lbcat.test_table_reference (id INT NOT NULL, test_column INT DEFAULT NULL NULL, CONSTRAINT PK_TEST_TABLE_REFERENCE PRIMARY KEY (id));

CREATE INDEX test_table_reference_index ON lbcat.test_table_reference(test_column);

ALTER TABLE lbcat.test_table_base ADD CONSTRAINT test_fk FOREIGN KEY (id) REFERENCES lbcat.test_table_reference (test_column) ON UPDATE RESTRICT ON DELETE CASCADE;

