--liquibase formatted sql

--changeset as:1
CREATE TABLE test_table_xml (test_column INTEGER);

--rollback DROP TABLE test_table_sql;