ALTER TABLE DATICAL_ADMIN.authors ADD varcharColumn VARCHAR2(25)
ALTER TABLE DATICAL_ADMIN.authors ADD intColumn INTEGER
ALTER TABLE DATICAL_ADMIN.authors ADD dateColumn date
UPDATE DATICAL_ADMIN.authors SET varcharColumn = 'INITIAL_VALUE'
UPDATE DATICAL_ADMIN.authors SET intColumn = 5
UPDATE DATICAL_ADMIN.authors SET dateColumn = TO_DATE('2020-09-21', 'YYYY-MM-DD')