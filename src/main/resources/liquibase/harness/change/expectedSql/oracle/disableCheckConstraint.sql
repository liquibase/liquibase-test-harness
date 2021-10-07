ALTER TABLE "C##LIQUIBASE".posts ADD CONSTRAINT test_check_constraint CHECK (id > 0)
ALTER TABLE "C##LIQUIBASE".posts DISABLE CONSTRAINT test_check_constraint