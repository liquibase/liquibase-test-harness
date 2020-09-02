--liquibase formatted sql
--changeset oleh:1
-- Database: postgresql
-- Change Parameter: newColumnName=first_name_renameColumn_test
-- Change Parameter: oldColumnName=first_name
-- Change Parameter: tableName=authors
ALTER TABLE authors RENAME COLUMN first_name TO first_name_renameColumn_test;
--rollback ALTER TABLE authors RENAME COLUMN first_name_renameColumn_test TO first_name;
