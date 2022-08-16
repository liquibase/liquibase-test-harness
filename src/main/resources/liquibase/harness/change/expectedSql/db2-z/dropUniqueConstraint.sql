ALTER TABLE "IBMUSER".authors ADD CONSTRAINT test_unique_constraint UNIQUE (email)
ALTER TABLE "IBMUSER".authors DROP CONSTRAINT test_unique_constraint