CREATE TABLE testdb:informix.oldnametable (test_id INT NOT NULL, test_column VARCHAR(50) NOT NULL, PRIMARY KEY (test_id))
RENAME TABLE testdb:informix.oldnametable TO newnametable