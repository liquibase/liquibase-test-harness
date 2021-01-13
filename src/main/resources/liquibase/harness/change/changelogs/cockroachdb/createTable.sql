--liquibase formatted sql

--changeset kristyl:1
create table test_table (
  test_id int not null primary key,
  test_column varchar(50)
);
--rollback DROP TABLE test_table