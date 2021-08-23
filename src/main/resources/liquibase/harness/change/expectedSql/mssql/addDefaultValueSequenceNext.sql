USE lbcat
GO
UPDATE DATABASECHANGELOGLOCK SET LOCKED = 1, LOCKEDBY = '5CD90139BV (192.168.192.1)', LOCKGRANTED = '2021-08-23T16:39:32.597' WHERE ID = 1 AND LOCKED = 0
GO
CREATE SEQUENCE test_sequence START WITH 1 INCREMENT BY 1 MINVALUE 1
GO
ALTER TABLE authors ADD sequence_referenced_column numeric(18, 0)
GO
ALTER TABLE authors ADD CONSTRAINT DF_authors_sequence_referenced_column DEFAULT NEXT VALUE FOR test_sequence FOR sequence_referenced_column
GO
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE, DEPLOYMENT_ID) VALUES ('1', 'as', 'liquibase/harness/change/changelogs/addDefaultValueSequenceNext.xml', GETDATE(), 1, '8:29d7e6a2e3cd9ec66fc1ffb0bc808a73', 'createSequence sequenceName=test_sequence addColumn tableName=authors addDefaultValue columnName=sequence_referenced_column, tableName=authors', '', 'EXECUTED', NULL, NULL, '4.4.2', '9725973345')
GO
UPDATE DATABASECHANGELOGLOCK SET LOCKED = 0, LOCKEDBY = NULL, LOCKGRANTED = NULL WHERE ID = 1
GO