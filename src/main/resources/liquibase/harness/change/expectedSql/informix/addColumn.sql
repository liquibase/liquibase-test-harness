ALTER TABLE testdb:informix.authors ADD varcharColumn VARCHAR(25)
ALTER TABLE testdb:informix.authors ADD intColumn INT
ALTER TABLE testdb:informix.authors ADD dateColumn date
UPDATE testdb:informix.authors SET varcharColumn = 'INITIAL_VALUE'
UPDATE testdb:informix.authors SET intColumn = 5
UPDATE testdb:informix.authors SET dateColumn = '2020-09-21'