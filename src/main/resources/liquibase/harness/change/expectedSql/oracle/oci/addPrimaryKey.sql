CREATE TABLE ADMIN.test_table_addpk (test_id INTEGER, test_column VARCHAR2(50))
ALTER TABLE ADMIN.test_table_addpk ADD CONSTRAINT pk_test_table_addpk PRIMARY KEY (test_id)