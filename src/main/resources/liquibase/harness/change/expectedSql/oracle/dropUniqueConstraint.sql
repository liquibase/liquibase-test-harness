ALTER TABLE "C##LIQUIBASE".authors ADD CONSTRAINT test_unique_constraint UNIQUE (email) DEFERRABLE INITIALLY DEFERRED
ALTER TABLE "C##LIQUIBASE".authors DROP CONSTRAINT test_unique_constraint DROP INDEX