SET GLOBAL log_bin_trust_function_creators = 1
DROP FUNCTION IF EXISTS test_function
CREATE FUNCTION test_function()
RETURNS VARCHAR(20)
BEGIN
RETURN 'Hello'
END
DROP FUNCTION lbcat.test_function