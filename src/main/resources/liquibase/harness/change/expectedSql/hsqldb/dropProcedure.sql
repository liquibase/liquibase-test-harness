CREATE PROCEDURE test_procedure(firstname VARCHAR(50), lastname VARCHAR(50), email VARCHAR(50))
MODIFIES SQL DATA
INSERT INTO AUTHORS VALUES (DEFAULT, firstname, lastname, email, null, CURRENT_TIMESTAMP)
DROP PROCEDURE test_procedure