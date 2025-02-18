if object_id(\'test_procedure\', \'p\') is null exec (\'create procedure test_procedure as select 1 a\');
ALTER PROCEDURE test_procedure
      AS;