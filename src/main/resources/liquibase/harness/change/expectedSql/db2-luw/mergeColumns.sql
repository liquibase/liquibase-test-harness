CREATE TABLE "DB2INST1".full_name_table (first_name VARCHAR(50), last_name VARCHAR(50))
INSERT INTO "DB2INST1".full_name_table (first_name) VALUES ('John')
UPDATE "DB2INST1".full_name_table SET last_name = 'Doe' WHERE first_name='John'
INSERT INTO "DB2INST1".full_name_table (first_name) VALUES ('Jane')
UPDATE "DB2INST1".full_name_table SET last_name = 'Doe' WHERE first_name='Jane'
ALTER TABLE "DB2INST1".full_name_table ADD full_name VARCHAR(255)
CALL SYSPROC.ADMIN_CMD ('REORG TABLE "DB2INST1".full_name_table')
UPDATE "DB2INST1".full_name_table SET full_name = first_name || ' ' || last_name
ALTER TABLE "DB2INST1".full_name_table DROP COLUMN first_name
CALL SYSPROC.ADMIN_CMD ('REORG TABLE "DB2INST1".full_name_table')
ALTER TABLE "DB2INST1".full_name_table DROP COLUMN last_name
CALL SYSPROC.ADMIN_CMD ('REORG TABLE "DB2INST1".full_name_table')