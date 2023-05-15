CREATE SEQUENCE ADMIN.test_sequence START WITH 1 INCREMENT BY 1 MINVALUE 1
ALTER TABLE ADMIN.authors ADD sequence_referenced_column NUMBER
ALTER TABLE ADMIN.authors MODIFY sequence_referenced_column DEFAULT ADMIN.test_sequence.nextval