CREATE TABLE "PUBLIC".authors_data AS SELECT DISTINCT email AS authors_email FROM "PUBLIC".authors WHERE email IS NOT NULL
ALTER TABLE "PUBLIC".authors_data ALTER COLUMN  authors_email SET NOT NULL
ALTER TABLE "PUBLIC".authors_data ADD PRIMARY KEY (authors_email)
ALTER TABLE "PUBLIC".authors ADD CONSTRAINT FK_AUTHORS_AUTHORS_DATA FOREIGN KEY (email) REFERENCES "PUBLIC".authors_data (authors_email)