--liquibase formatted sql
--changeset oleh:1
-- Database: mysql
CREATE TABLE oldnametable (test_id INT NOT NULL, test_column VARCHAR(50) NOT NULL, CONSTRAINT PK_TEST_TABLE PRIMARY KEY (test_id));
ALTER TABLE oldnametable RENAME newnametable;
--rollback DROP TABLE newnametable;
