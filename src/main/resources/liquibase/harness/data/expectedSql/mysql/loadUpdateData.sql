INSERT INTO authors (id, first_name, last_name, email, birthdate, added) VALUES (1, 'Adam', 'Gods', 'test1@example.com', '1000-02-27', '2000-02-04 02:32:00')
ON DUPLICATE KEY UPDATE first_name = 'Adam',last_name = 'Gods',email = 'test1@example.com',birthdate = '1000-02-27',added = '2000-02-04 02:32:00'
INSERT INTO authors (id, first_name, last_name, email, birthdate, added) VALUES (7, 'Noah', 'Lamekhs', 'test2@example.com', '2000-02-27', '1994-12-10 01:00:00')
ON DUPLICATE KEY UPDATE first_name = 'Noah',last_name = 'Lamekhs',email = 'test2@example.com',birthdate = '2000-02-27',added = '1994-12-10 01:00:00'
INSERT INTO authors (id, first_name, last_name, email, birthdate, added) VALUES (8, 'Muhammad', 'Ibn Abdullah', 'test3@example.com', '3000-02-27', '2000-12-10 01:00:00')
ON DUPLICATE KEY UPDATE first_name = 'Muhammad',last_name = 'Ibn Abdullah',email = 'test3@example.com',birthdate = '3000-02-27',added = '2000-12-10 01:00:00'