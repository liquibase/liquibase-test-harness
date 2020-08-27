--liquibase formatted sql
--changeset oleh:1
-- Database: postgresql
-- Change Parameter: newTableName=creators
-- Change Parameter: oldTableName=authors
ALTER TABLE authors RENAME TO creators;
--rollback ALTER TABLE creators RENAME TO authors;