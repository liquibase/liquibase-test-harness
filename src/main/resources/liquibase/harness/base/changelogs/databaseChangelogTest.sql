--liquibase formatted sql

--changeset as:1
CREATE TABLE test_table (test_column INT);

--rollback DROP TABLE test_table;