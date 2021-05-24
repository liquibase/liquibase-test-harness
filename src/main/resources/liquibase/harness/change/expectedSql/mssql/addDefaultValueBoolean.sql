ALTER TABLE authors ADD booleanColumn bit
ALTER TABLE authors ADD CONSTRAINT DF_authors_booleanColumn DEFAULT 1 FOR booleanColumn