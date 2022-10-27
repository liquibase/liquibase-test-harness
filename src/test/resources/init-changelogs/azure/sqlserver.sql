--liquibase formatted sql
--changeset liquibase:1 runAlways:true
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
VALUES ('1','Eileen','Lubowitz','ppaucek@example.org','1991-03-04','2004-05-30 02:08:25'),
('2','Tamia','Mayert','shansen@example.org','2016-03-27','2014-03-21 02:52:00'),
('3','Cyril','Funk','reynolds.godfrey@example.com','1988-04-21','2011-06-24 18:17:48'),
('4','Nicolas','Buckridge','xhoeger@example.net','2017-02-03','2019-04-22 02:04:41'),
('5','Jayden','Walter','lillian66@example.com','2010-02-27','1990-02-04 02:32:00')
GO

DROP TABLE IF EXISTS [dbo].[posts]
GO

CREATE TABLE [dbo].[posts](
    [id] [int] NOT NULL,
    [author_id] [int] NOT NULL,
    [title] [varchar](255) NULL,
    [description] [varchar](255) NULL,
    [content] [varchar](255) NULL,
    [inserted_date] [date] NULL
    ) ON [PRIMARY]
GO

INSERT INTO [dbo].[posts] ([id],[author_id],[title],[description],[content],[inserted_date])
VALUES ('1','1','temporibus','voluptatum','Fugit non et doloribus repudiandae.','2015-11-18'),
('2','2','ea','aut','Tempora molestias maiores provident molestiae sint possimus quasi.','1975-06-08'),
('3','3','illum','rerum','Delectus recusandae sit officiis dolor.','1975-02-25'),
('4','4','itaque','deleniti','Magni nam optio id recusandae.','2010-07-28'),
('5','5','ad','similique','Rerum tempore quis ut nesciunt qui excepturi est.','2006-10-09')
GO
