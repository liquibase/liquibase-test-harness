INVALID TEST
-- Cockroach DB v 20.1 requires an index to be created manually on both columns prior to a foreign_key is created for those columns.
-- Current changeset doesn't allow to create those indexes manually. Also Cockroach DB is not officially supported by Liquibase.
-- That's why it is marked as invalid test.