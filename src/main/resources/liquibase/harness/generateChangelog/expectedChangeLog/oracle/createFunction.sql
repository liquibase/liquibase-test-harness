-- liquibase formatted sql

-- changeset osashc:1670931778214-1 splitStatements:false
CREATE OR REPLACE FUNCTION test_function(id IN NUMBER)
                                                RETURN VARCHAR2 AS
                                                BEGIN
                                                RETURN '2';
                                                END;
/

