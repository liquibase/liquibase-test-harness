--liquibase formatted sql

--changeset as:1
CREATE TABLE test_table_sql (test_column INT);

--rollback DROP TABLE test_table_sql;