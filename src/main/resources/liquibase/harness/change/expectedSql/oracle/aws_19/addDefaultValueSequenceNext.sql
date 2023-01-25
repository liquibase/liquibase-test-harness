CREATE SEQUENCE LBUSER.test_sequence START WITH 1 INCREMENT BY 1 MINVALUE 1
ALTER TABLE LBUSER.authors ADD sequence_referenced_column NUMBER
ALTER TABLE LBUSER.authors MODIFY sequence_referenced_column DEFAULT LBUSER.test_sequence.nextval