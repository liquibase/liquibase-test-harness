USE lbcat
GO
UPDATE DATABASECHANGELOGLOCK SET LOCKED = 1, LOCKEDBY = '5CD90139BV (192.168.192.1)', LOCKGRANTED = '2021-08-23T16:41:11.507' WHERE ID = 1 AND LOCKED = 0
GO
CREATE TABLE sqltest (id int)
GO
insert into sqltest (id) values (1)
GO
insert into sqltest (id) values (2)
GO
insert into sqltest (id) values (3)
GO
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE, DEPLOYMENT_ID) VALUES ('sqlTest', 'kristyl', 'liquibase/harness/change/changelogs/sql.xml', GETDATE(), 1, '8:f1c8a21615d6107d741a93ef3afdfeac', 'createTable tableName=sqltest sql', 'Creates a table and inserts values into the table with actual SQL', 'EXECUTED', NULL, NULL, '4.4.2', '9726072025')
GO
UPDATE DATABASECHANGELOGLOCK SET LOCKED = 0, LOCKEDBY = NULL, LOCKGRANTED = NULL WHERE ID = 1
GO