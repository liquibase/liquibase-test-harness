ALTER TABLE "IBMUSER".posts ADD CONSTRAINT test_check_constraint CHECK (id > 0)
ALTER TABLE "IBMUSER".posts DROP CONSTRAINT test_check_constraint