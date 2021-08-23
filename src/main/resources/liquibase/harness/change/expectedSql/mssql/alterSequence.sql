USE lbcat
GO
UPDATE DATABASECHANGELOGLOCK SET LOCKED = 1, LOCKEDBY = '5CD90139BV (192.168.192.1)', LOCKGRANTED = '2021-08-23T16:39:51.666' WHERE ID = 1 AND LOCKED = 0
GO
CREATE SEQUENCE test_sequence START WITH 1 INCREMENT BY 1 MINVALUE 1
GO
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE, DEPLOYMENT_ID) VALUES ('1', 'as', 'liquibase/harness/change/changelogs/mssql/alterSequence.xml', GETDATE(), 1, '8:af0efa5fa854a07c6e3ec8c74b091a0c', 'createSequence sequenceName=test_sequence', '', 'EXECUTED', NULL, NULL, '4.4.2', '9725992214')
GO
ALTER SEQUENCE test_sequence INCREMENT BY 10 MINVALUE 1 MAXVALUE 371717
GO
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE, DEPLOYMENT_ID) VALUES ('2', 'as', 'liquibase/harness/change/changelogs/mssql/alterSequence.xml', GETDATE(), 2, '8:6dab5be07d8acfaa8754ac03eaab3b30', 'alterSequence sequenceName=test_sequence', '', 'EXECUTED', NULL, NULL, '4.4.2', '9725992214')
GO
UPDATE DATABASECHANGELOGLOCK SET LOCKED = 0, LOCKEDBY = NULL, LOCKGRANTED = NULL WHERE ID = 1
GO