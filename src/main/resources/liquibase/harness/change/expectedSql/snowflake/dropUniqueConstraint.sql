ALTER TABLE "PUBLIC".authors ADD CONSTRAINT test_unique_constraint UNIQUE (email)
ALTER TABLE "PUBLIC".authors DROP CONSTRAINT test_unique_constraint