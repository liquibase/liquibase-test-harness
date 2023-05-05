CREATE OR REPLACE PACKAGE test_package
AS
PROCEDURE test_procedure
END test_package
/
CREATE OR REPLACE PACKAGE BODY test_package
IS
PROCEDURE test_procedure IS
BEGIN END test_procedure
END test_package
/
DROP PACKAGE BODY ADMIN.test_package
DROP PACKAGE ADMIN.test_package