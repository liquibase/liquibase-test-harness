-- liquibase formatted sql

-- changeset liquibase:table-dim-dumm-address
-- USE SCHEMA "JNSCHEMA"
create or replace TABLE DIM_DUMM_ADDRESS (
ADDRESSID NUMBER(38,0) autoincrement,
STREETNAME VARCHAR(16777216),
CITY VARCHAR(100),
STATE VARCHAR(100),
POSTALCODE VARCHAR(50),
COUNTRY VARCHAR(100),
ADDRESSTYPE VARCHAR(50),
STATUS VARCHAR(50),
DUMMY VARCHAR(50)
);
--rollback drop table table-dim-dumm-address;