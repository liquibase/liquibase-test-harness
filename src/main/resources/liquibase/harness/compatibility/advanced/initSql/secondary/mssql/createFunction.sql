DROP FUNCTION IF EXISTS [secondary_function];
CREATE FUNCTION [secondary_function]()
    RETURNS VARCHAR(20)
AS
BEGIN
    RETURN 'Hello'
END;

CREATE FUNCTION [FUNCTION1]()
    RETURNS VARCHAR(100)
AS
BEGIN
    DECLARE @rtEmail VARCHAR
    SET @rtEmail='random@datical.com'
    RETURN @rtEmail
END;