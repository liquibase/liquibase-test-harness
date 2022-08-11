BEGIN ATOMIC
DECLARE v_reccount INTEGER
SET v_reccount = (SELECT COUNT(*) FROM "DB2INST1".authors WHERE id = 1)
IF v_reccount = 0 THEN
INSERT INTO "DB2INST1".authors (id, first_name, last_name, email, birthdate, added) VALUES (1, 'Adam', 'Gods', 'test1@example.com', DATE('1000-02-27'), TIMESTAMP('2000-02-04 02:32:00'))
ELSEIF v_reccount = 1 THEN
UPDATE "DB2INST1".authors SET added = TIMESTAMP('2000-02-04 02:32:00'), birthdate = DATE('1000-02-27'), email = 'test1@example.com', first_name = 'Adam', last_name = 'Gods' WHERE id = 1
END IF
END
BEGIN ATOMIC
DECLARE v_reccount INTEGER
SET v_reccount = (SELECT COUNT(*) FROM "DB2INST1".authors WHERE id = 7)
IF v_reccount = 0 THEN
INSERT INTO "DB2INST1".authors (id, first_name, last_name, email, birthdate, added) VALUES (7, 'Noah', 'Lamekhs', 'test2@example.com', DATE('2000-02-27'), TIMESTAMP('1994-12-10 01:00:00'))
ELSEIF v_reccount = 1 THEN
UPDATE "DB2INST1".authors SET added = TIMESTAMP('1994-12-10 01:00:00'), birthdate = DATE('2000-02-27'), email = 'test2@example.com', first_name = 'Noah', last_name = 'Lamekhs' WHERE id = 7
END IF
END
BEGIN ATOMIC
DECLARE v_reccount INTEGER
SET v_reccount = (SELECT COUNT(*) FROM "DB2INST1".authors WHERE id = 8)
IF v_reccount = 0 THEN
INSERT INTO "DB2INST1".authors (id, first_name, last_name, email, birthdate, added) VALUES (8, 'Muhammad', 'Ibn Abdullah', 'test3@example.com', DATE('3000-02-27'), TIMESTAMP('2000-12-10 01:00:00'))
ELSEIF v_reccount = 1 THEN
UPDATE "DB2INST1".authors SET added = TIMESTAMP('2000-12-10 01:00:00'), birthdate = DATE('3000-02-27'), email = 'test3@example.com', first_name = 'Muhammad', last_name = 'Ibn Abdullah' WHERE id = 8
END IF
END