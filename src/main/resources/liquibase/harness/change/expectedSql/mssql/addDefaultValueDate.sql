ALTER TABLE authors ADD dateTimeColumn datetime
ALTER TABLE authors ADD CONSTRAINT DF_authors_dateTimeColumn DEFAULT '2008-02-12T12:34:03' FOR dateTimeColumn