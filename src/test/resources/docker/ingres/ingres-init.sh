#!/usr/bin bash

createdb lbcat

sql +user=actian,actian iidbdb create user lbuser with password='LiquibasePass1';\g\q

sql +user=lbuser,LiquibasePass1 lbcat DROP TABLE IF EXISTS authors;\g
CREATE TABLE authors (
                         id INTEGER NOT NULL PRIMARY KEY,
                         first_name CHAR(50) NOT NULL,
                         last_name CHAR(50) NOT NULL,
                         email CHAR(100) NOT NULL,
                         birthdate DATE NOT NULL,
                         added TIMESTAMP NOT NULL DEFAULT current_timestamp
);\g

INSERT INTO authors VALUES ('1','Eileen','Lubowitz','ppaucek@example.org','1991-03-04','2004-05-30 02:08:25'),
('2','Tamia','Mayert','shansen@example.org','2016-03-27','2014-03-21 02:52:00'),
('3','Cyril','Funk','reynolds.godfrey@example.com','1988-04-21','2011-06-24 18:17:48'),
('4','Nicolas','Buckridge','xhoeger@example.net','2017-02-03','2019-04-22 02:04:41'),
('5','Jayden','Walter','lillian66@example.com','2010-02-27','1990-02-04 02:32:00');\g

DROP TABLE IF EXISTS posts;\g

CREATE TABLE posts (
                       id INTEGER NOT NULL,
                       author_id INTEGER NOT NULL,
                       title CHAR(255) NOT NULL,
                       description CHAR(500) NOT NULL,
                       content TEXT(500) NOT NULL,
                       inserted_date DATE
);\g

INSERT INTO posts VALUES ('1','1','temporibus','voluptatum','Fugit non et doloribus repudiandae.','2015-11-18'),
('2','2','ea','aut','Tempora molestias maiores provident molestiae sint possimus quasi.','1975-06-08'),
('3','3','illum','rerum','Delectus recusandae sit officiis dolor.','1975-02-25'),
('4','4','itaque','deleniti','Magni nam optio id recusandae.','2010-07-28'),
('5','5','ad','similique','Rerum tempore quis ut nesciunt qui excepturi est.','2006-10-09');\g\q



