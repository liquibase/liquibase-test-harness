CREATE FUNCTION test_function (X1 DOUBLE) RETURNS DOUBLE DETERMINISTIC RETURN SIN(X1)/COS(X1)
DROP FUNCTION "IBMUSER".test_function