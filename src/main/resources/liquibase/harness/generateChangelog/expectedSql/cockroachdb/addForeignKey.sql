CREATE TABLE "test_table_base" ("id" BIGINT NOT NULL, CONSTRAINT "test_table_base_pkey" PRIMARY KEY ("id"));

CREATE TABLE "test_table_reference" ("id" BIGINT NOT NULL, "test_column" BIGINT NOT NULL, CONSTRAINT "test_table_reference_pkey" PRIMARY KEY ("id"));

ALTER TABLE "test_table_reference" ADD CONSTRAINT "test_table_reference_unique" UNIQUE ("test_column");

ALTER TABLE "test_table_base" ADD CONSTRAINT "test_fk" FOREIGN KEY ("id") REFERENCES "test_table_reference" ("test_column") ON UPDATE RESTRICT ON DELETE CASCADE;