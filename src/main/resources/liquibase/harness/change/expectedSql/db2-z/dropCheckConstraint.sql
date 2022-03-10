ALTER TABLE "LTHUSER".posts ADD CONSTRAINT test_check_constraint CHECK (id > 0)
ALTER TABLE "LTHUSER".posts DROP CONSTRAINT test_check_constraint