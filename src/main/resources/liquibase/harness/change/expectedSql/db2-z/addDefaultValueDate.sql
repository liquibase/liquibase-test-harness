CREATE TABLE "IBMUSER".add_default_value_date_test (id INTEGER NOT NULL, date_test TIMESTAMP, CONSTRAINT PK_ADD_DEFAULT_VA PRIMARY KEY (id))
ALTER TABLE "IBMUSER".add_default_value_date_test ALTER COLUMN  date_test SET DEFAULT TIMESTAMP('2008-02-12 12:34:03')