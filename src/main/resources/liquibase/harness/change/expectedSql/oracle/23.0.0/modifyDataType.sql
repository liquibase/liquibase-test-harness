CREATE TABLE LIQUIBASE.modify_data_type_test (intColumn INTEGER, dateColumn date)
ALTER TABLE LIQUIBASE.modify_data_type_test MODIFY intColumn VARCHAR2(50)