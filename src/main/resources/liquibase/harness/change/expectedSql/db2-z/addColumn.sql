CREATE TABLE "LTHUSER".add_column_test (id INTEGER NOT NULL, CONSTRAINT PK_ADD_COLUMN_TEST PRIMARY KEY (id))
ALTER TABLE "LTHUSER".add_column_test ADD varcharColumn VARCHAR(25)
ALTER TABLE "LTHUSER".add_column_test ADD intColumn INTEGER
ALTER TABLE "LTHUSER".add_column_test ADD dateColumn date
UPDATE "LTHUSER".add_column_test SET varcharColumn = 'INITIAL_VALUE'
UPDATE "LTHUSER".add_column_test SET intColumn = 5
UPDATE "LTHUSER".add_column_test SET dateColumn = DATE('2020-09-21')