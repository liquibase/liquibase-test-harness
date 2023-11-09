CREATE FUNCTION `test_function`() RETURNS varchar(20) CHARSET utf8mb4
DETERMINISTIC
RETURN 'Hello'
DROP FUNCTION secondary_function