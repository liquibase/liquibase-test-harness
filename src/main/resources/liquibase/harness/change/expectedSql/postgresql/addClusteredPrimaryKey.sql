CREATE TABLE public.tbl_cluster (id numeric(4) NOT NULL)
ALTER TABLE public.tbl_cluster ADD CONSTRAINT pk_clustered PRIMARY KEY (id)
CLUSTER public.tbl_cluster USING pk_clustered
