CREATE TABLE tbl_cluster (id numeric(4) NOT NULL)
ALTER TABLE tbl_cluster ADD CONSTRAINT pk_clustered PRIMARY KEY (id)
CLUSTER tbl_cluster USING pk_clustered
