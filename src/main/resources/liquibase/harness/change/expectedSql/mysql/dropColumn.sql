ALTER TABLE posts ADD varcharColumn VARCHAR(25) NULL
UPDATE posts SET varcharColumn = 'INITIAL_VALUE'
ALTER TABLE posts DROP COLUMN varcharColumn