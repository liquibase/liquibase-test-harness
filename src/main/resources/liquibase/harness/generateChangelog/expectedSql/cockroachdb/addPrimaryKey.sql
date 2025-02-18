CREATE TABLE public."primaryKeyTest" (test_id INTEGER NOT NULL, test_column VARCHAR(50))
ALTER TABLE public."primaryKeyTest" ADD CONSTRAINT "primary" PRIMARY KEY (test_id)