-- liquibase formatted sql

-- changeset osashc:1670931778214-1 splitStatements:false
if object_id('test_function') is null exec ('CREATE FUNCTION test_function()
                                                RETURNS VARCHAR(20)
                                                BEGIN
                                                RETURN ''Hello'';
                                                END') else exec ('ALTER FUNCTION test_function()
                                                RETURNS VARCHAR(20)
                                                BEGIN
                                                RETURN ''Hello'';
                                                END');