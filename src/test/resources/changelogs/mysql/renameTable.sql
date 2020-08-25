--liquibase formatted sql
--changeset oleh:1
-- Database: mysql
-- Change Parameter: newTableName=creators
-- Change Parameter: oldTableName=authors
ALTER TABLE authors RENAME creators;
--rollback ALTER TABLE creators RENAME authors;
