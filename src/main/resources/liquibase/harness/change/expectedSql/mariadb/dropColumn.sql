ALTER TABLE lbcat.posts ADD varcharColumn VARCHAR(25) NULL
UPDATE lbcat.posts SET varcharColumn = 'INITIAL_VALUE'
ALTER TABLE lbcat.posts DROP COLUMN varcharColumn