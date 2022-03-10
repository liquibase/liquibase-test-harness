INVALID TEST

--Bug: Generated query is incorrect due to absence of RESTRICT keyword at the end. TODO: remove ignoring after release of https://github.com/liquibase/liquibase/pull/2243

--Correct excpected sql is:
--CREATE TABLE "LTHUSER".drop_column_test (id INTEGER NOT NULL, varcharColumn VARCHAR(25), CONSTRAINT PK_DROP_COLUMN_TE PRIMARY KEY (id))
--ALTER TABLE "LTHUSER".drop_column_test DROP COLUMN varcharColumn RESTRICT