--liquibase formatted sql
--changeset liquibase:1 runAlways:true failOnError:false

USE lbcat;

EXEC sp_adduser 'lbuser';

EXEC sp_addrolemember 'db_owner', 'lbuser'