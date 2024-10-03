CREATE OR REPLACE FUNCTION test_function()
returns float
as '3.141592654::FLOAT'
<<<<<<< HEAD
DROP FUNCTION "public".test_function()
=======
DROP FUNCTION PUBLIC.test_function()
>>>>>>> main
