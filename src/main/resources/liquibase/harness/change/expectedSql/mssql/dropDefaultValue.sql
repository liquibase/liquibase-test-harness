USE lbcat
GO
UPDATE DATABASECHANGELOGLOCK SET LOCKED = 1, LOCKEDBY = '5CD90139BV (192.168.192.1)', LOCKGRANTED = '2021-08-23T16:40:21.242' WHERE ID = 1 AND LOCKED = 0
GO
ALTER TABLE posts ADD CONSTRAINT DF_posts_title DEFAULT 'title_test' FOR title
GO
DECLARE @sql [nvarchar](MAX)
SELECT @sql = N'ALTER TABLE posts DROP CONSTRAINT ' + QUOTENAME([df].[name]) FROM [sys].[columns] AS [c] INNER JOIN [sys].[default_constraints] AS [df] ON [df].[object_id] = [c].[default_object_id] WHERE [c].[object_id] = OBJECT_ID(N'posts') AND [c].[name] = N'title'
EXEC sp_executesql @sql
GO
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE, DEPLOYMENT_ID) VALUES ('1', 'oleh', 'liquibase/harness/change/changelogs/dropDefaultValue.xml', GETDATE(), 1, '8:fee9233aa702219218205d1b879a78dd', 'addDefaultValue columnName=title, tableName=posts dropDefaultValue columnName=title, tableName=posts', '', 'EXECUTED', NULL, NULL, '4.4.2', '9726021804')
GO
UPDATE DATABASECHANGELOGLOCK SET LOCKED = 0, LOCKEDBY = NULL, LOCKGRANTED = NULL WHERE ID = 1
GO