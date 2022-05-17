CREATE or replace PROCEDURE test_procedure()
RETURNS VARCHAR
LANGUAGE javascript
AS
$$
var rs = snowflake.execute( { sqlText:
`INSERT INTO table1 ("column 1")
SELECT 'value 1' AS "column 1" ;`
} )
return 'Done.'
$$