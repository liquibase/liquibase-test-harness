CREATE TABLE "test_table" ("id" BIGINT, "rowid" BIGINT DEFAULT unique_rowid() NOT NULL, CONSTRAINT "primary" PRIMARY KEY ("rowid"));

ALTER TABLE "test_table" ADD CONSTRAINT "test_unique_constraint" UNIQUE ("id");