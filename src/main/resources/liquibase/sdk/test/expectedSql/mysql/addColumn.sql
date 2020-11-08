ALTER TABLE authors ADD varcharColumn VARCHAR(25) NULL, ADD intColumn INT NULL, ADD dateColumn date NULL
UPDATE authors SET varcharColumn = 'INITIAL_VALUE'
UPDATE authors SET intColumn = 5
UPDATE authors SET dateColumn = '2020-09-21'