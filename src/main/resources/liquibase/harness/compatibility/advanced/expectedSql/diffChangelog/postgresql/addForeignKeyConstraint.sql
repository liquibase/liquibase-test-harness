ALTER TABLE "test_table_reference" DROP CONSTRAINT "secondary_test_fk"
ALTER TABLE "test_table_base" ADD CONSTRAINT "test_fk" FOREIGN KEY ("id") REFERENCES "test_table_reference" ("test_column") ON UPDATE NO ACTION ON DELETE NO ACTION
ALTER TABLE "test_table_base" DROP CONSTRAINT "test_table_base_test_column_key"