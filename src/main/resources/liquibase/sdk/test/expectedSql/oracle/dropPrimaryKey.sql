CREATE TABLE test_table_droppk (test_id INTEGER, test_column VARCHAR2(50))
ALTER TABLE test_table_droppk ADD CONSTRAINT pk_test_table_droppk PRIMARY KEY (test_id)
ALTER TABLE test_table_droppk DROP PRIMARY KEY DROP INDEX