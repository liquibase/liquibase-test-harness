-- This test was created to reproduce a Liquibase bug. [AS] syntax is not supported by postgesql in versions prior to 10
-- https://github.com/liquibase/liquibase/issues/1429
-- same apply to EDB 9.5 we put INVALID TEST for this not to break a build until issue is fixed
INVALID TEST
CREATE SEQUENCE  IF NOT EXISTS test_sequence START WITH 1 INCREMENT BY 1 MINVALUE 1
