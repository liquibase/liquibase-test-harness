ALTER TABLE LTHDB."PUBLIC".authors ADD varcharColumn VARCHAR(25)
ALTER TABLE LTHDB."PUBLIC".authors ADD intColumn INT
ALTER TABLE LTHDB."PUBLIC".authors ADD dateColumn date
UPDATE LTHDB."PUBLIC".authors SET varcharColumn = 'INITIAL_VALUE'
UPDATE LTHDB."PUBLIC".authors SET intColumn = 5
UPDATE LTHDB."PUBLIC".authors SET dateColumn = '2020-09-21'