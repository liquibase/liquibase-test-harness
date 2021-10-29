if object_id('dbo.test_function') is null exec ('CREATE FUNCTION test_function()
RETURNS VARCHAR(20)
BEGIN
RETURN ''Hello''
END') else exec ('ALTER FUNCTION test_function()
RETURNS VARCHAR(20)
BEGIN
RETURN ''Hello''
END')
DROP FUNCTION test_function