CREATE SEQUENCE LIQUIBASE.test_sequence START WITH 1 INCREMENT BY 1 MINVALUE 1
ALTER TABLE LIQUIBASE.authors ADD sequence_referenced_column NUMBER
ALTER TABLE LIQUIBASE.authors MODIFY sequence_referenced_column DEFAULT LIQUIBASE.test_sequence.nextval