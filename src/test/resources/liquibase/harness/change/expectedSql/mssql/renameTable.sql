CREATE TABLE oldnametable (test_id int NOT NULL, test_column varchar(50) NOT NULL, CONSTRAINT PK_OLDNAMETABLE PRIMARY KEY (test_id))
exec sp_rename 'oldnametable', 'newnametable'