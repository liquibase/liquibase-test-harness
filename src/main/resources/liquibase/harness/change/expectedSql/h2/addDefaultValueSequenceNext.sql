CREATE SEQUENCE test_sequence START WITH 1 INCREMENT BY 1 MINVALUE 1
ALTER TABLE authors ADD sequence_referenced_column NUMBER
ALTER TABLE authors ALTER COLUMN  sequence_referenced_column SET DEFAULT NEXTVAL('test_sequence')