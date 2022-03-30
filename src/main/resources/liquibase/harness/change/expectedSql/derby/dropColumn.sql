ALTER TABLE APP.posts ADD varcharColumn VARCHAR(25)
UPDATE APP.posts SET varcharColumn = 'INITIAL_VALUE'
ALTER TABLE APP.posts DROP COLUMN varcharColumn