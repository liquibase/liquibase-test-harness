-- This test was created to reproduce a Liquibase bug. renameSequence doesn't work as it depends on creteSequence
-- https://github.com/liquibase/liquibase/issues/1429
CREATE SEQUENCE  IF NOT EXISTS test_sequence AS int START WITH 1 INCREMENT BY 1 MINVALUE 1
ALTER SEQUENCE test_sequence RENAME TO test_sequence_rename_test
