CREATE TABLE LTHDB."PUBLIC".dropDefaultTest (id INT, stringColumn STRING DEFAULT 'testDefault')
ALTER TABLE LTHDB."PUBLIC".dropDefaultTest ALTER COLUMN stringColumn DROP DEFAULT