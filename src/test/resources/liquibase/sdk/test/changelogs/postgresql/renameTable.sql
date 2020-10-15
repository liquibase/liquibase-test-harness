--liquibase formatted sql
--changeset oleh:1
-- Database: postgresql
CREATE TABLE oldnametable (test_id INTEGER NOT NULL, test_column VARCHAR(50) NOT NULL, CONSTRAINT OLDNAMETABLE_PKEY PRIMARY KEY (test_id));
ALTER TABLE oldnametable RENAME TO newnametable
--rollback DROP TABLE newnametable;
