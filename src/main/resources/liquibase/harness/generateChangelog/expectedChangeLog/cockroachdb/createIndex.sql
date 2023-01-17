CREATE TABLE "test_table" ("id" BIGINT, "rowid" BIGINT DEFAULT unique_rowid() NOT NULL, CONSTRAINT "test_table_pkey" PRIMARY KEY ("rowid"));

CREATE INDEX "idx_first_name" ON "test_table"("id");