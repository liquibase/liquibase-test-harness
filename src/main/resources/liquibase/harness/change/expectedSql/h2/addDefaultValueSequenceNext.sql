CREATE SEQUENCE PUBLIC.test_sequence START WITH 1 INCREMENT BY 1 MINVALUE 1
ALTER TABLE PUBLIC.authors ADD sequence_referenced_column NUMBER
ALTER TABLE PUBLIC.authors ALTER COLUMN  sequence_referenced_column SET DEFAULT NEXTVAL('PUBLIC.test_sequence')