--liquibase formatted sql

--changeset kristyl:1
create table cockroachdb_table (
  id_column int not null primary key,
  name_column varchar(255)
);
--rollback DROP TABLE cockroachdb_table