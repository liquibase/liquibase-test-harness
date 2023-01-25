CREATE TABLE TEST_TABLE (ID VARCHAR(50) NOT NULL, TEST_COLUMN VARCHAR(50));

CREATE TRIGGER test_trigger
            AFTER UPDATE OF test_table.id
            ON test_table REFERENCING OLD AS OLD NEW AS NEW
            FOR EACH ROW MODE DB2SQL
            UPDATE test_table SET test_column = 'New description'
            WHERE test_column IS NULL