-- This test was created to reproduce a Liquibase bug. [AS] syntax is not supported by postgesql in versions prior to 10
-- https://github.com/liquibase/liquibase/issues/1429
CREATE SEQUENCE  IF NOT EXISTS test_sequence AS int START WITH 1 INCREMENT BY 1 MINVALUE 1
