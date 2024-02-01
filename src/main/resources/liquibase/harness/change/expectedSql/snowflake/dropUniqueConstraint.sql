ALTER TABLE "public".authors ADD CONSTRAINT test_unique_constraint UNIQUE (email)
ALTER TABLE "public".authors DROP CONSTRAINT test_unique_constraint