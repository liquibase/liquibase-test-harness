CREATE TABLE public."test_table" ("id" INTEGER);

ALTER TABLE public."test_table" ADD CONSTRAINT "test_unique_constraint" UNIQUE ("id");