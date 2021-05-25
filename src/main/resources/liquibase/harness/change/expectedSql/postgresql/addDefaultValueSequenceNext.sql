CREATE SEQUENCE  IF NOT EXISTS test_sequence START WITH 1 INCREMENT BY 1 MINVALUE 1
ALTER TABLE authors ADD sequence_referenced_column numeric
ALTER TABLE authors ALTER COLUMN  sequence_referenced_column SET DEFAULT nextval('test_sequence')
ALTER SEQUENCE test_sequence OWNED BY authors.sequence_referenced_column