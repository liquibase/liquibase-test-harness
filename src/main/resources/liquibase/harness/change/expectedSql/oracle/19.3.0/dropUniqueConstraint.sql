ALTER TABLE DATICAL_ADMIN.authors ADD CONSTRAINT test_unique_constraint UNIQUE (email) DEFERRABLE INITIALLY DEFERRED
ALTER TABLE DATICAL_ADMIN.authors DROP CONSTRAINT test_unique_constraint DROP INDEX