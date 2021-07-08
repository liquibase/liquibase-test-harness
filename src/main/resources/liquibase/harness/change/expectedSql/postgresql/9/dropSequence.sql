-- This test was created to reproduce a Liquibase bug. dropSequence doesn't work as it depends on creteSequence
-- https://github.com/liquibase/liquibase/issues/1429
CREATE SEQUENCE  IF NOT EXISTS test_sequence AS int START WITH 1 INCREMENT BY 1 MINVALUE 1
DROP SEQUENCE test_sequence CASCADE
