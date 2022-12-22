--liquibase formatted sql

--changeset as:1 -context:test_context -labels:test_label
--comment: test_comment
CREATE TABLE test_table_sql (test_column INT);

--rollback DROP TABLE test_table_sql;