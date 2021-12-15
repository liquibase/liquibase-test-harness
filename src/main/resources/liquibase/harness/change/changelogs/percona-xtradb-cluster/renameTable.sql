--liquibase formatted sql
--changeset oleh:1
-- Database: mysql
CREATE TABLE oldnametable (test_id INT NOT NULL, test_column VARCHAR(50) NOT NULL, CONSTRAINT PK_TEST_TABLE PRIMARY KEY (test_id));
ALTER TABLE oldnametable RENAME lbcat.newnametable;
--rollback DROP TABLE lbcat.newnametable;
