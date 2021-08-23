CREATE TABLE full_name_table (first_name varchar(50), last_name varchar(50))
INSERT INTO full_name_table (first_name) VALUES ('John')
UPDATE full_name_table SET last_name = 'Doe' WHERE first_name='John'
INSERT INTO full_name_table (first_name) VALUES ('Jane')
UPDATE full_name_table SET last_name = 'Doe' WHERE first_name='Jane'
ALTER TABLE full_name_table ADD full_name varchar(255)
UPDATE full_name_table SET full_name = first_name + ' ' + last_name
DECLARE @sql [nvarchar](MAX)
SELECT @sql = N'ALTER TABLE full_name_table DROP CONSTRAINT ' + QUOTENAME([df].[name]) FROM [sys].[columns] AS [c] INNER JOIN [sys].[default_constraints] AS [df] ON [df].[object_id] = [c].[default_object_id] WHERE [c].[object_id] = OBJECT_ID(N'full_name_table') AND [c].[name] = N'first_name'
EXEC sp_executesql @sql
ALTER TABLE full_name_table DROP COLUMN first_name
DECLARE @sql [nvarchar](MAX)
SELECT @sql = N'ALTER TABLE full_name_table DROP CONSTRAINT ' + QUOTENAME([df].[name]) FROM [sys].[columns] AS [c] INNER JOIN [sys].[default_constraints] AS [df] ON [df].[object_id] = [c].[default_object_id] WHERE [c].[object_id] = OBJECT_ID(N'full_name_table') AND [c].[name] = N'last_name'
EXEC sp_executesql @sql
ALTER TABLE full_name_table DROP COLUMN last_name