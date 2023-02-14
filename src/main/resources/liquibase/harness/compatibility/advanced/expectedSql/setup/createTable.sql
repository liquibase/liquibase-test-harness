--liquibase formatted sql

--changeset as:1
CREATE TABLE lbcat.test_table_xml (test_column INT NULL);

--rollback DROP TABLE test_table_sql;