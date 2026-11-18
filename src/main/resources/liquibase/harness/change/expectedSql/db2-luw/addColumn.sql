CREATE TABLE "DB2INST1".add_column_test (id INTEGER NOT NULL, CONSTRAINT PK_ADD_COLUMN_TEST PRIMARY KEY (id))
ALTER TABLE "DB2INST1".add_column_test ADD varcharColumn VARCHAR(25)
ALTER TABLE "DB2INST1".add_column_test ADD intColumn INTEGER
ALTER TABLE "DB2INST1".add_column_test ADD dateColumn date
CALL SYSPROC.ADMIN_CMD ('REORG TABLE "DB2INST1".add_column_test')
UPDATE "DB2INST1".add_column_test SET varcharColumn = 'INITIAL_VALUE'
UPDATE "DB2INST1".add_column_test SET intColumn = 5
UPDATE "DB2INST1".add_column_test SET dateColumn = DATE('2020-09-21')