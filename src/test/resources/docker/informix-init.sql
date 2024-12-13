CREATE DATABASE testdb WITH LOG MODE ANSI;
DATABASE testdb;

DROP TABLE IF EXISTS testdb:informix.authors;
CREATE TABLE testdb:informix.authors (
                         id SERIAL PRIMARY KEY,
                         first_name VARCHAR(50) NOT NULL,
                         last_name VARCHAR(50) NOT NULL,
                         email VARCHAR(100) NOT NULL,
                         birthdate DATE NOT NULL,
                         added DATETIME YEAR TO FRACTION(5) DEFAULT CURRENT YEAR TO FRACTION(5)
);

INSERT INTO testdb:informix.authors (id, first_name, last_name, email, birthdate, added)
VALUES
    (1, 'Eileen', 'Lubowitz', 'ppaucek@example.org', '1991-03-04', '2004-05-30 02:08:25');

INSERT INTO testdb:informix.authors (id, first_name, last_name, email, birthdate, added)
VALUES
    (2, 'Tamia', 'Mayert', 'shansen@example.org', '2016-03-27', '2014-03-21 02:52:00');

INSERT INTO testdb:informix.authors (id, first_name, last_name, email, birthdate, added)
VALUES
    (3, 'Cyril', 'Funk', 'reynolds.godfrey@example.com', '1988-04-21', '2011-06-24 18:17:48');

INSERT INTO testdb:informix.authors (id, first_name, last_name, email, birthdate, added)
VALUES
    (4, 'Nicolas', 'Buckridge', 'xhoeger@example.net', '2017-02-03', '2019-04-22 02:04:41');

INSERT INTO testdb:informix.authors (id, first_name, last_name, email, birthdate, added)
VALUES
    (5, 'Jayden', 'Walter', 'lillian66@example.com', '2010-02-27', '1990-02-04 02:32:00');


DROP TABLE IF EXISTS testdb:informix.posts;
CREATE TABLE testdb:informix.posts (
                       id SERIAL,
                       author_id INT NOT NULL,
                       title VARCHAR(255) NOT NULL,
                       description VARCHAR(255) NOT NULL,
                       content VARCHAR(255) NOT NULL,
                       inserted_date DATE
);

ALTER TABLE testdb:informix.posts MODIFY (title VARCHAR(255) DEFAULT 'title_test');


INSERT INTO testdb:informix.posts (id, author_id, title, description, content, inserted_date)
VALUES  (1,1,'temporibus','voluptatum','Fugit non et doloribus repudiandae.','2015-11-18');

INSERT INTO testdb:informix.posts (id, author_id, title, description, content, inserted_date)
VALUES  (2,2,'ea','aut','Tempora molestias maiores provident molestiae sint possimus quasi.','1975-06-08');

INSERT INTO testdb:informix.posts (id, author_id, title, description, content, inserted_date)
VALUES  (3,3,'illum','rerum','Delectus recusandae sit officiis dolor.','1975-02-25');

INSERT INTO testdb:informix.posts (id, author_id, title, description, content, inserted_date)
VALUES  (4,4,'itaque','deleniti','Magni nam optio id recusandae.','2010-07-28');

INSERT INTO testdb:informix.posts (id, author_id, title, description, content, inserted_date)
VALUES  (5,5,'ad','similique','Rerum tempore quis ut nesciunt qui excepturi est.','2006-10-09');