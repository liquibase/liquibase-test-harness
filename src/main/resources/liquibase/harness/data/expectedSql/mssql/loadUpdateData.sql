DECLARE @reccount integer
SELECT @reccount = count(*) FROM authors WHERE id = 1
IF @reccount = 0
BEGIN
INSERT INTO authors (id, first_name, last_name, email, birthdate, added) VALUES (1, 'Adam', 'Gods', 'test1@example.com', '1000-02-27', '2000-02-04T02:32:00')
END
ELSE
BEGIN
UPDATE authors SET added = '2000-02-04T02:32:00', birthdate = '1000-02-27', email = 'test1@example.com', first_name = 'Adam', last_name = 'Gods' WHERE id = 1
END
DECLARE @reccount integer
SELECT @reccount = count(*) FROM authors WHERE id = 7
IF @reccount = 0
BEGIN
INSERT INTO authors (id, first_name, last_name, email, birthdate, added) VALUES (7, 'Noah', 'Lamekhs', 'test2@example.com', '2000-02-27', '1994-12-10T01:00:00')
END
ELSE
BEGIN
UPDATE authors SET added = '1994-12-10T01:00:00', birthdate = '2000-02-27', email = 'test2@example.com', first_name = 'Noah', last_name = 'Lamekhs' WHERE id = 7
END
DECLARE @reccount integer
SELECT @reccount = count(*) FROM authors WHERE id = 8
IF @reccount = 0
BEGIN
INSERT INTO authors (id, first_name, last_name, email, birthdate, added) VALUES (8, 'Muhammad', 'Ibn Abdullah', 'test3@example.com', '3000-02-27', '2000-12-10T01:00:00')
END
ELSE
BEGIN
UPDATE authors SET added = '2000-12-10T01:00:00', birthdate = '3000-02-27', email = 'test3@example.com', first_name = 'Muhammad', last_name = 'Ibn Abdullah' WHERE id = 8
END