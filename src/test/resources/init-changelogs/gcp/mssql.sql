--liquibase formatted sql
--changeset liquibase:1 runAlways:true

USE lbcat;

IF NOT EXISTS (SELECT * FROM sys.sysusers WHERE name = 'lbuser')
BEGIN
    EXEC sp_adduser 'lbuser';
END

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'lbuser' AND type_desc = 'DATABASE_ROLE' AND is_fixed_role = 0)
BEGIN
    EXEC sp_addrolemember 'db_owner', 'lbuser';
END