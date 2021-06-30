CREATE TABLE authors_data AS SELECT DISTINCT email AS authors_email FROM authors WHERE email IS NOT NULL
ALTER TABLE authors_data MODIFY authors_email VARCHAR(100) COLLATE utf8_unicode_ci NOT NULL
ALTER TABLE authors_data ADD PRIMARY KEY (authors_email)
ALTER TABLE authors ADD CONSTRAINT FK_AUTHORS_AUTHORS_DATA FOREIGN KEY (email) REFERENCES authors_data (authors_email)