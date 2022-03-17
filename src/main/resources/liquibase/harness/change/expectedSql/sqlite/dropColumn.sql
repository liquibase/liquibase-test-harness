INVALID TEST

Bug. Ignored until the issue https://github.com/liquibase/liquibase/issues/2644 is solved.

-- CREATE TABLE test_table (test_id INTEGER NOT NULL, test_column VARCHAR(50) NOT NULL, CONSTRAINT PK_TEST_TABLE PRIMARY KEY (test_id))
-- ALTER TABLE test_table RENAME TO test_table_temporary
-- CREATE TABLE test_table ()
-- DROP TABLE test_table_temporary
-- REINDEX test_table