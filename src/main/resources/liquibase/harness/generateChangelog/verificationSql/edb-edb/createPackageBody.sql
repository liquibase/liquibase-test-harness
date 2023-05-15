CREATE OR REPLACE PACKAGE test_package IS
            PROCEDURE test_procedure();
            END;

CREATE OR REPLACE PACKAGE BODY test_package IS
            PROCEDURE test_procedure() IS
            BEGIN
            DELETE FROM posts WHERE author_id = '15';
            END;
            END