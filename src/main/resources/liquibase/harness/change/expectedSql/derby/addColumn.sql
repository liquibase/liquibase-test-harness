ALTER TABLE APP.authors ADD varcharColumn VARCHAR(25)
ALTER TABLE APP.authors ADD intColumn INTEGER
ALTER TABLE APP.authors ADD dateColumn date
UPDATE APP.authors SET varcharColumn = 'INITIAL_VALUE'
UPDATE APP.authors SET intColumn = 5
UPDATE APP.authors SET dateColumn = DATE('2020-09-21')