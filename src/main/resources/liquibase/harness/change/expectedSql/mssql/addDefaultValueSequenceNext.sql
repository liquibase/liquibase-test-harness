CREATE SEQUENCE test_sequence START WITH 1 INCREMENT BY 1 MINVALUE 1
ALTER TABLE authors ADD sequence_referenced_column numeric(18, 0)
ALTER TABLE authors ADD CONSTRAINT DF_authors_sequence_referenced_column DEFAULT NEXT VALUE FOR test_sequence FOR sequence_referenced_column