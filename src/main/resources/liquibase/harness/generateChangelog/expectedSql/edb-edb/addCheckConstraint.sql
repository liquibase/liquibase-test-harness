ALTER TABLE "posts" ADD CONSTRAINT "test_check_constraint" CHECK (((id > 0)));