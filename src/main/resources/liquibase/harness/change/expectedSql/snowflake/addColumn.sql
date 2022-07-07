ALTER TABLE "PUBLIC".authors ADD varcharColumn VARCHAR(25)
ALTER TABLE "PUBLIC".authors ADD intColumn INT
ALTER TABLE "PUBLIC".authors ADD dateColumn date
UPDATE "PUBLIC".authors SET varcharColumn = 'INITIAL_VALUE'
UPDATE "PUBLIC".authors SET intColumn = 5
UPDATE "PUBLIC".authors SET dateColumn = '2020-09-21'