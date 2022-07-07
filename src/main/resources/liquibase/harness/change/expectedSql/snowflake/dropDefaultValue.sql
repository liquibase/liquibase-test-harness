CREATE TABLE "PUBLIC".dropDefaultTest (id INT, stringColumn STRING DEFAULT 'testDefault')
ALTER TABLE "PUBLIC".dropDefaultTest ALTER COLUMN stringColumn DROP DEFAULT