CREATE TABLE "IBMUSER".add_column_test (id INTEGER NOT NULL, CONSTRAINT PK_ADD_COLUMN_TEST PRIMARY KEY (id))
ALTER TABLE "IBMUSER".add_column_test ADD varcharColumn VARCHAR(25)
ALTER TABLE "IBMUSER".add_column_test ADD intColumn INTEGER
ALTER TABLE "IBMUSER".add_column_test ADD dateColumn date
UPDATE "IBMUSER".add_column_test SET varcharColumn = 'INITIAL_VALUE'
UPDATE "IBMUSER".add_column_test SET intColumn = 5
UPDATE "IBMUSER".add_column_test SET dateColumn = DATE('2020-09-21')