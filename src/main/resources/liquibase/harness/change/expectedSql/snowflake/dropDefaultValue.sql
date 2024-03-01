CREATE TABLE "public".dropDefaultTest (id INT, stringColumn STRING DEFAULT 'testDefault')
ALTER TABLE "public".dropDefaultTest ALTER COLUMN stringColumn DROP DEFAULT