ALTER TABLE posts ADD CONSTRAINT DF_posts_title DEFAULT 'title_test' FOR title
DECLARE @sql [nvarchar](MAX)
SELECT @sql = N'ALTER TABLE posts DROP CONSTRAINT ' + QUOTENAME([df].[name]) FROM [sys].[columns] AS [c] INNER JOIN [sys].[default_constraints] AS [df] ON [df].[object_id] = [c].[default_object_id] WHERE [c].[object_id] = OBJECT_ID(N'posts') AND [c].[name] = N'title'
EXEC sp_executesql @sql