-- liquibase formatted sql

-- changeset osashc:1670931778214-1 splitStatements:false
CREATE FUNCTION `test_function`() RETURNS varchar(20) CHARSET utf8mb4
BEGIN
                                                RETURN 'Hello';
                                                END;

