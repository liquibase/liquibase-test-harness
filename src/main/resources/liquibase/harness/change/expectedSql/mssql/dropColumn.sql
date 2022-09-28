ALTER TABLE posts ADD varcharColumn varchar(25)
UPDATE posts SET varcharColumn = 'INITIAL_VALUE'
DECLARE @sql [nvarchar](MAX)
SELECT @sql = N'ALTER TABLE posts DROP CONSTRAINT ' + QUOTENAME([df].[name]) FROM [sys].[columns] AS [c] INNER JOIN [sys].[default_constraints] AS [df] ON [df].[object_id] = [c].[default_object_id] WHERE [c].[object_id] = OBJECT_ID(N'posts') AND [c].[name] = N'varcharColumn'
    EXEC sp_executesql @sql
ALTER TABLE posts DROP COLUMN varcharColumn