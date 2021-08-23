USE lbcat
GO
UPDATE DATABASECHANGELOGLOCK SET LOCKED = 1, LOCKEDBY = '5CD90139BV (192.168.192.1)', LOCKGRANTED = '2021-08-23T16:41:08.678' WHERE ID = 1 AND LOCKED = 0
GO
DECLARE @TableName SYSNAME set @TableName = N'authors' DECLARE @FullTableName SYSNAME SET @FullTableName = N'dbo.authors'DECLARE @MS_DescriptionValue NVARCHAR(3749) SET @MS_DescriptionValue = N'A Test Remark'DECLARE @MS_Description NVARCHAR(3749) set @MS_Description = NULL SET @MS_Description = (SELECT CAST(Value AS NVARCHAR(3749)) AS [MS_Description] FROM sys.extended_properties AS ep WHERE ep.major_id = OBJECT_ID(@FullTableName) AND ep.name = N'MS_Description' AND ep.minor_id=0) IF @MS_Description IS NULL BEGIN EXEC sys.sp_addextendedproperty @name  = N'MS_Description', @value = @MS_DescriptionValue, @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = @TableName END ELSE BEGIN EXEC sys.sp_updateextendedproperty @name  = N'MS_Description', @value = @MS_DescriptionValue, @level0type = N'SCHEMA', @level0name = N'dbo', @level1type = N'TABLE', @level1name = @TableName END
GO
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE, DEPLOYMENT_ID) VALUES ('1', 'as', 'liquibase/harness/change/changelogs/mssql/setTableRemarks.xml', GETDATE(), 1, '8:671049b5a1569ad50a5aa9ab81ecd760', 'setTableRemarks tableName=authors', '', 'EXECUTED', NULL, NULL, '4.4.2', '9726069305')
GO
UPDATE DATABASECHANGELOGLOCK SET LOCKED = 0, LOCKEDBY = NULL, LOCKGRANTED = NULL WHERE ID = 1
GO