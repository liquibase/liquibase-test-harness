ALTER TABLE DATICAL_ADMIN.posts ADD varcharColumn VARCHAR2(25)
UPDATE DATICAL_ADMIN.posts SET varcharColumn = 'INITIAL_VALUE'
ALTER TABLE DATICAL_ADMIN.posts DROP COLUMN varcharColumn