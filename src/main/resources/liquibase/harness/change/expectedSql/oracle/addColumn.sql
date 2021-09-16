ALTER TABLE "C##LIQUIBASE".authors ADD varcharColumn VARCHAR2(25)
ALTER TABLE "C##LIQUIBASE".authors ADD intColumn INTEGER
ALTER TABLE "C##LIQUIBASE".authors ADD dateColumn date
UPDATE "C##LIQUIBASE".authors SET varcharColumn = 'INITIAL_VALUE'
UPDATE "C##LIQUIBASE".authors SET intColumn = 5
UPDATE "C##LIQUIBASE".authors SET dateColumn = TO_DATE('2020-09-21', 'YYYY-MM-DD')