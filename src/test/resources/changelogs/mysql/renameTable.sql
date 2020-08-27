--liquibase formatted sql
--changeset oleh:1
-- Database: mysql
CREATE TABLE lbcat.oldnametable (test_id INT NOT NULL, test_column VARCHAR(50) NOT NULL, CONSTRAINT PK_TEST_TABLE PRIMARY KEY (test_id));
ALTER TABLE lbcat.oldnametable RENAME lbcat.newnametable;
--rollback DROP TABLE lbcat.newnametable;
