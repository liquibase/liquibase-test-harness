USE lbcat
GO
UPDATE DATABASECHANGELOGLOCK SET LOCKED = 1, LOCKEDBY = '5CD90139BV (192.168.192.1)', LOCKGRANTED = '2021-08-23T16:40:49.836' WHERE ID = 1 AND LOCKED = 0
GO
CREATE TABLE full_name_table (first_name varchar(50), last_name varchar(50))
GO
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE, DEPLOYMENT_ID) VALUES ('1', 'as', 'liquibase/harness/change/changelogs/mergeColumns.xml', GETDATE(), 1, '8:3b795fd89fd023ec76e5300fd3e44c7e', 'createTable tableName=full_name_table', '', 'EXECUTED', NULL, NULL, '4.4.2', '9726050387')
GO
INSERT INTO full_name_table (first_name) VALUES ('John')
GO
UPDATE full_name_table SET last_name = 'Doe' WHERE first_name='John'
GO
INSERT INTO full_name_table (first_name) VALUES ('Jane')
GO
UPDATE full_name_table SET last_name = 'Doe' WHERE first_name='Jane'
GO
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE, DEPLOYMENT_ID) VALUES ('2', 'as', 'liquibase/harness/change/changelogs/mergeColumns.xml', GETDATE(), 2, '8:0bde5d83fe9171b2f357c8c9e8bea905', 'insert tableName=full_name_table update tableName=full_name_table insert tableName=full_name_table update tableName=full_name_table', '', 'EXECUTED', NULL, NULL, '4.4.2', '9726050387')
GO
ALTER TABLE full_name_table ADD full_name varchar(255)
GO
UPDATE full_name_table SET full_name = first_name + ' ' + last_name
GO
DECLARE @sql [nvarchar](MAX)
SELECT @sql = N'ALTER TABLE full_name_table DROP CONSTRAINT ' + QUOTENAME([df].[name]) FROM [sys].[columns] AS [c] INNER JOIN [sys].[default_constraints] AS [df] ON [df].[object_id] = [c].[default_object_id] WHERE [c].[object_id] = OBJECT_ID(N'full_name_table') AND [c].[name] = N'first_name'
EXEC sp_executesql @sql
GO
ALTER TABLE full_name_table DROP COLUMN first_name
GO
DECLARE @sql [nvarchar](MAX)
SELECT @sql = N'ALTER TABLE full_name_table DROP CONSTRAINT ' + QUOTENAME([df].[name]) FROM [sys].[columns] AS [c] INNER JOIN [sys].[default_constraints] AS [df] ON [df].[object_id] = [c].[default_object_id] WHERE [c].[object_id] = OBJECT_ID(N'full_name_table') AND [c].[name] = N'last_name'
EXEC sp_executesql @sql
GO
ALTER TABLE full_name_table DROP COLUMN last_name
GO
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE, DEPLOYMENT_ID) VALUES ('3', 'as', 'liquibase/harness/change/changelogs/mergeColumns.xml', GETDATE(), 3, '8:f9ad6817667197b19b02db5d67b101f1', 'mergeColumns column1Name=first_name, column2Name=last_name, finalColumnName=full_name, tableName=full_name_table', '', 'EXECUTED', NULL, NULL, '4.4.2', '9726050387')
GO
UPDATE DATABASECHANGELOGLOCK SET LOCKED = 0, LOCKEDBY = NULL, LOCKGRANTED = NULL WHERE ID = 1
GO