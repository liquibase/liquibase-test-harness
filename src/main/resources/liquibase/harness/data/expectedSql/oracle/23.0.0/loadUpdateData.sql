DECLARE
v_reccount NUMBER := 0
BEGIN
SELECT COUNT(*) INTO v_reccount FROM LIQUIBASE.authors WHERE id = 1
IF v_reccount = 0 THEN
INSERT INTO LIQUIBASE.authors (id, first_name, last_name, email, birthdate, added) VALUES (1, 'Adam', 'Gods', 'test1@example.com', TO_DATE('1000-02-27', 'YYYY-MM-DD'), TO_DATE('2000-02-04 02:32:00', 'YYYY-MM-DD HH24:MI:SS'))
ELSIF v_reccount = 1 THEN
UPDATE LIQUIBASE.authors SET added = TO_DATE('2000-02-04 02:32:00', 'YYYY-MM-DD HH24:MI:SS'), birthdate = TO_DATE('1000-02-27', 'YYYY-MM-DD'), email = 'test1@example.com', first_name = 'Adam', last_name = 'Gods' WHERE id = 1
END IF
END
DECLARE
v_reccount NUMBER := 0
BEGIN
SELECT COUNT(*) INTO v_reccount FROM LIQUIBASE.authors WHERE id = 7
IF v_reccount = 0 THEN
INSERT INTO LIQUIBASE.authors (id, first_name, last_name, email, birthdate, added) VALUES (7, 'Noah', 'Lamekhs', 'test2@example.com', TO_DATE('2000-02-27', 'YYYY-MM-DD'), TO_DATE('1994-12-10 01:00:00', 'YYYY-MM-DD HH24:MI:SS'))
ELSIF v_reccount = 1 THEN
UPDATE LIQUIBASE.authors SET added = TO_DATE('1994-12-10 01:00:00', 'YYYY-MM-DD HH24:MI:SS'), birthdate = TO_DATE('2000-02-27', 'YYYY-MM-DD'), email = 'test2@example.com', first_name = 'Noah', last_name = 'Lamekhs' WHERE id = 7
END IF
END
DECLARE
v_reccount NUMBER := 0
BEGIN
SELECT COUNT(*) INTO v_reccount FROM LIQUIBASE.authors WHERE id = 8
IF v_reccount = 0 THEN
INSERT INTO LIQUIBASE.authors (id, first_name, last_name, email, birthdate, added) VALUES (8, 'Muhammad', 'Ibn Abdullah', 'test3@example.com', TO_DATE('3000-02-27', 'YYYY-MM-DD'), TO_DATE('2000-12-10 01:00:00', 'YYYY-MM-DD HH24:MI:SS'))
ELSIF v_reccount = 1 THEN
UPDATE LIQUIBASE.authors SET added = TO_DATE('2000-12-10 01:00:00', 'YYYY-MM-DD HH24:MI:SS'), birthdate = TO_DATE('3000-02-27', 'YYYY-MM-DD'), email = 'test3@example.com', first_name = 'Muhammad', last_name = 'Ibn Abdullah' WHERE id = 8
END IF
END