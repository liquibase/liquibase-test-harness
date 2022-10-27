ALTER TABLE "DB2INST1".posts ADD CONSTRAINT test_check_constraint CHECK (id > 0)
ALTER TABLE "DB2INST1".posts DROP CONSTRAINT test_check_constraint