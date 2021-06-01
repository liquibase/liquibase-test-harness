CREATE SEQUENCE test_sequence START WITH 1 INCREMENT BY 1 MINVALUE 1
ALTER TABLE authors ADD sequence_referenced_column NUMBER
ALTER TABLE authors MODIFY sequence_referenced_column DEFAULT test_sequence.nextval