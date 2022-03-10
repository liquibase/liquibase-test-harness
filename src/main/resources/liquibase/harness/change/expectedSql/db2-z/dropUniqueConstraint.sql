ALTER TABLE "LTHUSER".authors ADD CONSTRAINT test_unique_constraint UNIQUE (email)
ALTER TABLE "LTHUSER".authors DROP CONSTRAINT test_unique_constraint