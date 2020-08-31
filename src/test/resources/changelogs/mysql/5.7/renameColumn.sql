--liquibase formatted sql
--changeset oleh:1
-- Database: mysql
-- Change Parameter: columnDataType=varchar
-- Change Parameter: newColumnName=name
-- Change Parameter: oldColumnName=first_name
-- Change Parameter: tableName=authors
ALTER TABLE authors CHANGE first_name first_name_renameColumn_test varchar(50) COLLATE utf8_unicode_ci NOT NULL;
--rollback ALTER TABLE authors CHANGE first_name_renameColumn_test first_name varchar(50) COLLATE utf8_unicode_ci NOT NULL;