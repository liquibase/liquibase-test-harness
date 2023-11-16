ALTER TABLE test_table ADD varcharColumn varchar(25)
ALTER TABLE test_table ADD intColumn int
ALTER TABLE test_table ADD dateColumn date
DECLARE @sql [nvarchar](MAX)
SELECT @sql = N'ALTER TABLE test_table DROP CONSTRAINT ' + QUOTENAME([df].[name]) FROM [sys].[columns] AS [c] INNER JOIN [sys].[default_constraints] AS [df] ON [df].[object_id] = [c].[default_object_id] WHERE [c].[object_id] = OBJECT_ID(N'test_table') AND [c].[name] = N'secondary_column'
EXEC sp_executesql @sql
ALTER TABLE test_table DROP COLUMN secondary_column