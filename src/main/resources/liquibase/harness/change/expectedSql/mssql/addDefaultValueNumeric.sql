ALTER TABLE authors ADD numericColumn numeric(18, 0)
ALTER TABLE authors ADD CONSTRAINT DF_authors_numericColumn DEFAULT 100000000 FOR numericColumn