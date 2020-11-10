--liquibase formatted sql

--changeset kristyl:3
create view test_view as select id, first_name, last_name, email from lbcat.authors;
--rollback drop view test_view