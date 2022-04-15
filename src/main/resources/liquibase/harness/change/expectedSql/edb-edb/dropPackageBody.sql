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
DROP PACKAGE BODY public.test_package
DROP PACKAGE public.test_package