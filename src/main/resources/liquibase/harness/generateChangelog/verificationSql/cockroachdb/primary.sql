CREATE TABLE test_table (id BIGINT NOT NULL, rowid BIGINT DEFAULT unique_rowid() NOT NULL, CONSTRAINT "test_pk" PRIMARY KEY (id));