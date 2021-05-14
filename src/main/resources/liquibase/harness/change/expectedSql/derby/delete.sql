CREATE TABLE testdelete_table (test_column VARCHAR(50))
INSERT INTO testdelete_table VALUES ('mike was here')
DELETE FROM testdelete_table WHERE test_column='mike was here'