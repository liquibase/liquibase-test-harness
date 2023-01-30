-- liquibase formatted sql

-- changeset osashc:1670931778214-1 splitStatements:false
CREATE FUNCTION `test_function`() RETURNS varchar(20) CHARSET latin1 COLLATE latin1_swedish_ci
BEGIN
                                                RETURN 'Hello';
                                                END;

