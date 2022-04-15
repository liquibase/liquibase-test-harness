CREATE OR REPLACE PACKAGE test_package
AS
PROCEDURE test_procedure
END test_package
CREATE OR REPLACE PACKAGE BODY test_package AS
PROCEDURE test_procedure()
AS
BEGIN
DELETE FROM posts WHERE author_id = '15'
END
END