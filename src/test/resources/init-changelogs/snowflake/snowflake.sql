-- liquibase formatted sql
-- changeset liquibase:1 runAlways:true
DROP WAREHOUSE IF EXISTS LTHDB_TEST_WAREHOUSE;
DROP WAREHOUSE IF EXISTS LTHDB_TEST_MULTICLUSTER_WH;
DROP WAREHOUSE IF EXISTS LTHDB_TEST_ADVANCED_WH;

-- changeset liquibase:2 runAlways:true
-- Resume and use the main test warehouse if it exists and is suspended
ALTER WAREHOUSE IF EXISTS LTHDB_TEST_WH RESUME IF SUSPENDED;
USE WAREHOUSE LTHDB_TEST_WH;

-- changeset liquibase:3 runAlways:true
DROP TABLE IF EXISTS AUTHORS;
CREATE TABLE AUTHORS (
                         id int AUTOINCREMENT(6,1) NOT NULL,
                         first_name VARCHAR(50) NOT NULL,
                         last_name varchar(50) NOT NULL,
                         email varchar(100) NOT NULL,
                         birthdate date NOT NULL,
                         added timestamp NOT NULL DEFAULT current_timestamp(),
                         PRIMARY KEY (id)
);

-- changeset liquibase:4 runAlways:true
INSERT INTO AUTHORS VALUES ('1','Eileen','Lubowitz','ppaucek@example.org','1991-03-04','2004-05-30 02:08:25'),
                           ('2','Tamia','Mayert','shansen@example.org','2016-03-27','2014-03-21 02:52:00'),
                           ('3','Cyril','Funk','reynolds.godfrey@example.com','1988-04-21','2011-06-24 18:17:48'),
                           ('4','Nicolas','Buckridge','xhoeger@example.net','2017-02-03','2019-04-22 02:04:41'),
                           ('5','Jayden','Walter','lillian66@example.com','2010-02-27','1990-02-04 02:32:00');

-- changeset liquibase:5 runAlways:true
DROP TABLE IF EXISTS POSTS;
CREATE TABLE POSTS (
                       id int NOT NULL,
                       author_id int NOT NULL,
                       title varchar(255) NOT NULL,
                       description varchar(500) NOT NULL,
                       content text NOT NULL,
                       inserted_date date
);

-- changeset liquibase:6 runAlways:true
INSERT INTO POSTS VALUES ('1','1','temporibus','voluptatum','Fugit non et doloribus repudiandae.','2015-11-18'),
                         ('2','2','ea','aut','Tempora molestias maiores provident molestiae sint possimus quasi.','1975-06-08'),
                         ('3','3','illum','rerum','Delectus recusandae sit officiis dolor.','1975-02-25'),
                         ('4','4','itaque','deleniti','Magni nam optio id recusandae.','2010-07-28'),
                         ('5','5','ad','similique','Rerum tempore quis ut nesciunt qui excepturi est.','2006-10-09');

-- changeset liquibase:7 runAlways:true
-- Ensure we're using the main warehouse for subsequent operations
USE WAREHOUSE LTHDB_TEST_WH;