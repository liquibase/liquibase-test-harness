CREATE DATABASE testdb WITH LOG MODE ANSI;

DATABASE testdb;

-- Use BEGIN WORK to start a transaction
BEGIN WORK;

-- Drop and create authors table
DROP TABLE IF EXISTS authors;
CREATE TABLE authors (
                         id SERIAL PRIMARY KEY,
                         first_name VARCHAR(50) NOT NULL,
                         last_name VARCHAR(50) NOT NULL,
                         email VARCHAR(100) NOT NULL,
                         birthdate DATE NOT NULL,
                         added DATETIME YEAR TO FRACTION(5) DEFAULT CURRENT YEAR TO FRACTION(5)
);

-- Insert data into authors table
INSERT INTO authors (id, first_name, last_name, email, birthdate, added)
VALUES
    (1, 'Eileen', 'Lubowitz', 'ppaucek@example.org',
     TO_DATE('1991-03-04', '%Y-%m-%d'),
     TO_DATE('2004-05-30 02:08:25', '%Y-%m-%d %H:%M:%S'));

INSERT INTO authors (id, first_name, last_name, email, birthdate, added)
VALUES
    (2, 'Tamia', 'Mayert', 'shansen@example.org',
     TO_DATE('2016-03-27', '%Y-%m-%d'),
     TO_DATE('2014-03-21 02:52:00', '%Y-%m-%d %H:%M:%S'));
INSERT INTO authors (id, first_name, last_name, email, birthdate, added)
VALUES
    (3, 'Cyril', 'Funk', 'reynolds.godfrey@example.com',
     TO_DATE('1988-04-21', '%Y-%m-%d'),
     TO_DATE('2011-06-24 18:17:48', '%Y-%m-%d %H:%M:%S'));

INSERT INTO authors (id, first_name, last_name, email, birthdate, added)
VALUES
    (4, 'Nicolas', 'Buckridge', 'xhoeger@example.net',
     TO_DATE('2017-02-03', '%Y-%m-%d'),
     TO_DATE('2019-04-22 02:04:41', '%Y-%m-%d %H:%M:%S'));

INSERT INTO authors (id, first_name, last_name, email, birthdate, added)
VALUES
    (5, 'Jayden', 'Walter', 'lillian66@example.com',
     TO_DATE('2010-02-27', '%Y-%m-%d'),
     TO_DATE('1990-02-04 02:32:00', '%Y-%m-%d %H:%M:%S'));

-- Commit the transaction for authors table
COMMIT WORK;

-- Start a new transaction for the posts table
BEGIN WORK;

-- Drop and create posts table
DROP TABLE IF EXISTS posts;
CREATE TABLE posts (
                       id SERIAL PRIMARY KEY,
                       author_id INT NOT NULL,
                       title VARCHAR(255) NOT NULL DEFAULT 'title_test',
                       description VARCHAR(255) NOT NULL,
                       content VARCHAR(255) NOT NULL,
                       inserted_date DATE NOT NULL
);

-- Insert data into posts table
INSERT INTO posts (id, author_id, title, description, content, inserted_date)
VALUES
    (1, 1, 'temporibus', 'voluptatum', 'Fugit non et doloribus repudiandae.',
     TO_DATE('2015-11-18', '%Y-%m-%d'));

INSERT INTO posts (id, author_id, title, description, content, inserted_date)
VALUES
    (2, 2, 'ea', 'aut', 'Tempora molestias maiores provident molestiae sint possimus quasi.',
     TO_DATE('1975-06-08', '%Y-%m-%d'));

INSERT INTO posts (id, author_id, title, description, content, inserted_date)
VALUES
    (3, 3, 'illum', 'rerum', 'Delectus recusandae sit officiis dolor.',
     TO_DATE('1975-02-25', '%Y-%m-%d'));

INSERT INTO posts (id, author_id, title, description, content, inserted_date)
VALUES
    (4, 4, 'itaque', 'deleniti', 'Magni nam optio id recusandae.',
     TO_DATE('2010-07-28', '%Y-%m-%d'));

INSERT INTO posts (id, author_id, title, description, content, inserted_date)
VALUES
    (5, 5, 'ad', 'similique', 'Rerum tempore quis ut nesciunt qui excepturi est.',
     TO_DATE('2006-10-09', '%Y-%m-%d'));

-- Commit the transaction for posts table
COMMIT WORK;