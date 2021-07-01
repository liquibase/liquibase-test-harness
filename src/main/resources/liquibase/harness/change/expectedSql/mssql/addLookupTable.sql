SELECT DISTINCT email AS authors_email INTO authors_data FROM authors WHERE email IS NOT NULL
ALTER TABLE authors_data ALTER COLUMN authors_email varchar(100) NOT NULL
ALTER TABLE authors_data ADD PRIMARY KEY (authors_email)
ALTER TABLE authors ADD CONSTRAINT FK_AUTHORS_AUTHORS_DATA FOREIGN KEY (email) REFERENCES authors_data (authors_email)