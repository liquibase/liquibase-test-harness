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
DROP PACKAGE BODY DATICAL_ADMIN.test_package
DROP PACKAGE DATICAL_ADMIN.test_package