CREATE SEQUENCE "C##LIQUIBASE".test_sequence START WITH 1 INCREMENT BY 1 MINVALUE 1
ALTER TABLE "C##LIQUIBASE".authors ADD sequence_referenced_column NUMBER
ALTER TABLE "C##LIQUIBASE".authors MODIFY sequence_referenced_column DEFAULT "C##LIQUIBASE".test_sequence.nextval