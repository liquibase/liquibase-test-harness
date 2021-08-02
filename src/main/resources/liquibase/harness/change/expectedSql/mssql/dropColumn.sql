INVALID TEST
-- Actually this test case has a bug and is marked INVALID in order to avoid ignoring whole platform at Github actions tests
-- https://github.com/liquibase/liquibase/issues/1818

ALTER TABLE posts ADD varcharColumn varchar(25)
UPDATE posts SET varcharColumn = 'INITIAL_VALUE'
ALTER TABLE posts DROP COLUMN varcharColumn