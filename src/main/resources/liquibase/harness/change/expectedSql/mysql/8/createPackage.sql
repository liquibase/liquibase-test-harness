SET GLOBAL log_bin_trust_function_creators = 1
CREATE FUNCTION test_function()
RETURNS VARCHAR(20)
BEGIN
RETURN 'Hello'
END