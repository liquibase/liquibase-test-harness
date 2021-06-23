DECLARE @dataPath varchar(256);
DECLARE @logPath varchar(256);
SET @dataPath=(SELECT CAST(serverproperty('InstanceDefaultDataPath') AS varchar(256)));
SET @logPath=(SELECT CAST(serverproperty('InstanceDefaultLogPath') AS varchar(256)));

CREATE LOGIN [lbuser] with password=N'LiquibasePass1', CHECK_EXPIRATION=OFF;
GO

CREATE DATABASE lbcat;
GO

EXEC lbcat..sp_addsrvrolemember @loginame = N'lbuser', @rolename = N'sysadmin'
GO

/* By default, we set the compatibility level to the oldest version we are officially supporting. Note that there
 * are differences in behaviour, e.g. with implicit conversions of date and time values. See
 * https://docs.microsoft.com/en-us/sql/t-sql/functions/cast-and-convert-transact-sql for details.
 */
ALTER DATABASE [lbcat] SET COMPATIBILITY_LEVEL = 100
GO

USE [lbcat]
GO
ALTER DATABASE [lbcat] MODIFY FILEGROUP [PRIMARY] DEFAULT
GO
ALTER DATABASE [lbcat] ADD FILEGROUP [liquibase2]
GO

DECLARE @dataPath varchar(256);
DECLARE @logPath varchar(256);
SET @dataPath=(SELECT CAST(serverproperty('InstanceDefaultDataPath') AS varchar(256)));
SET @logPath=(SELECT CAST(serverproperty('InstanceDefaultLogPath') AS varchar(256)));

DECLARE @createSql varchar(2000);
SET @createSql = (SELECT 'ALTER DATABASE [lbcat] ADD FILE ( NAME = N''liquibase2'', FILENAME = N''' + @dataPath + 'liquibase2.ndf'' , SIZE = 8192KB , FILEGROWTH = 65536KB ) TO FILEGROUP [liquibase2]');
EXECUTE(@createSql);
GO

DROP TABLE IF EXISTS [dbo].[authors]
GO

CREATE TABLE [dbo].[authors](
    [id] [int] NOT NULL,
    [first_name] [varchar](255) NOT NULL,
    [last_name] [varchar](255) NULL,
    [email] [varchar](100) NULL,
    [birthdate] [varchar](255) NULL,
    [added] [datetime] NOT NULL,
    CONSTRAINT [PK_PrimaryKey] PRIMARY KEY ([id])
    ) ON [PRIMARY]
GO

INSERT INTO [dbo].[authors] ([id],[first_name],[last_name],[email],[birthdate],[added])
VALUES ('1','Courtney','Hodkiewicz','borer.edison@example.org','1986-01-22','1983-08-23 14:55:09'),
('2','Marielle','Kuhlman','llakin@example.org','1995-08-08','1984-03-05 01:25:02'),
('3','Emmanuel','Gleichner','jean.zemlak@example.net','1997-05-09','1977-08-09 10:28:04'),
('4','Hertha','Goodwin','hollis.gusikowski@example.org','2014-08-21','2009-01-28 11:02:56'),
('5','Ewald','Sauer','juvenal35@example.com','1988-10-10','2000-11-02 00:37:53')
GO

DROP TABLE IF EXISTS [dbo].[posts]
GO

CREATE TABLE [dbo].[posts](
    [id] [int] NOT NULL,
    [author_id] [varchar](255) NOT NULL,
    [title] [varchar](255) NULL,
    [description] [varchar](255) NULL,
    [content] [varchar](255) NULL,
    [inserted_date] [date] NULL
    ) ON [PRIMARY]
GO

INSERT INTO [dbo].[posts] ([id],[author_id],[title],[description],[content],[inserted_date])
VALUES ('1','1','sit','in','At corporis est sint beatae beatae.','1996-05-04'),
('2','2','nisi','et','Sunt nemo magni et tenetur debitis blanditiis.','2000-05-25'),
('3','3','ratione','blanditiis','Ipsa distinctio doloremque et ut.','1997-09-22'),
('4','4','ad','et','Repudiandae porro explicabo officiis sed quis voluptate et.','1978-12-13'),
('5','5','deserunt','temporibus','Mollitia reiciendis debitis est voluptatem est neque.','1979-12-06')
GO

CREATE SCHEMA [lbcat2] AUTHORIZATION [dbo]
GO
