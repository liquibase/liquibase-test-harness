ALTER TABLE ADMIN.authors ADD varcharColumn VARCHAR2(25)
ALTER TABLE ADMIN.authors ADD intColumn INTEGER
ALTER TABLE ADMIN.authors ADD dateColumn date
UPDATE ADMIN.authors SET varcharColumn = 'INITIAL_VALUE'
UPDATE ADMIN.authors SET intColumn = 5
UPDATE ADMIN.authors SET dateColumn = TO_DATE('2020-09-21', 'YYYY-MM-DD')