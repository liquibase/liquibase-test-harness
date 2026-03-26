ALTER TABLE testdb:informix.posts ADD varcharColumn VARCHAR(25)
UPDATE testdb:informix.posts SET varcharColumn = 'INITIAL_VALUE'
ALTER TABLE testdb:informix.posts DROP varcharColumn