CREATE TABLE testdb:informix.authors_data ( authors_email varchar(100) )
INSERT INTO testdb:informix.authors_data ( authors_email ) SELECT DISTINCT email FROM testdb:informix.authors WHERE email IS NOT NULL
ALTER TABLE testdb:informix.authors_data MODIFY (authors_email VARCHAR(100) NOT NULL)
ALTER TABLE testdb:informix.authors_data ADD CONSTRAINT PRIMARY KEY (authors_email)
ALTER TABLE testdb:informix.authors ADD CONSTRAINT  FOREIGN KEY (email) REFERENCES testdb:informix.authors_data (authors_email) CONSTRAINT FK_AUTHORS_AUTHORS_DATA