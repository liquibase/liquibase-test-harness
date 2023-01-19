--liquibase formatted sql

--changeset as:1
CREATE TABLE TEST_TABLE_XML (TEST_COLUMN INTEGER);

--rollback DROP TABLE test_table_sql;