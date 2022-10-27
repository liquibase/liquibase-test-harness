CREATE TABLE "DB2INST1".modify_data_type_test (intColumn INTEGER, dateColumn date)
ALTER TABLE "DB2INST1".modify_data_type_test ALTER COLUMN intColumn SET DATA TYPE VARCHAR(50)
CALL SYSPROC.ADMIN_CMD ('REORG TABLE "DB2INST1".modify_data_type_test')