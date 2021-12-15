ALTER TABLE lbcat.authors ADD varcharColumn VARCHAR(25) NULL, ADD intColumn INT NULL, ADD dateColumn date NULL
UPDATE lbcat.authors SET varcharColumn = 'INITIAL_VALUE'
UPDATE lbcat.authors SET intColumn = 5
UPDATE lbcat.authors SET dateColumn = '2020-09-21'