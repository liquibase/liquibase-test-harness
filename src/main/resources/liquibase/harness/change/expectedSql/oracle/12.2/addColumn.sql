ALTER TABLE LBUSER.authors ADD varcharColumn VARCHAR2(25)
ALTER TABLE LBUSER.authors ADD intColumn INTEGER
ALTER TABLE LBUSER.authors ADD dateColumn date
UPDATE LBUSER.authors SET varcharColumn = 'INITIAL_VALUE'
UPDATE LBUSER.authors SET intColumn = 5
UPDATE LBUSER.authors SET dateColumn = TO_DATE('2020-09-21', 'YYYY-MM-DD')