CREATE TABLE oldnametable (test_id INT NOT NULL, test_column VARCHAR(50) NOT NULL, CONSTRAINT PK_OLDNAMETABLE PRIMARY KEY (test_id))
ALTER TABLE oldnametable RENAME TO newnametable