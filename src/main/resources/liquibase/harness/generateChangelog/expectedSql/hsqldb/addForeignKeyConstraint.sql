CREATE TABLE test_table_base (id int NOT NULL, CONSTRAINT PK_TEST_TABLE_BASE PRIMARY KEY (id));

CREATE TABLE test_table_reference (id int, test_column int NOT NULL, CONSTRAINT PK_TEST_TABLE_REFERENCE PRIMARY KEY (test_column));

ALTER TABLE test_table_base ADD CONSTRAINT test_fk FOREIGN KEY (id) REFERENCES test_table_reference (test_column) ON UPDATE NO ACTION ON DELETE CASCADE;

