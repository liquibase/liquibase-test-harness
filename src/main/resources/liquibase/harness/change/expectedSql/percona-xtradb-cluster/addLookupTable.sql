INVALID TEST

Percona-XtraDB-Cluster prohibits use of CREATE TABLE AS SELECT with pxc_strict_mode = ENFORCING or MASTER [Failed SQL: (1105) CREATE TABLE lbcat.authors_data AS SELECT DISTINCT email AS authors_email FROM lbcat.authors WHERE email IS NOT NULL]

--CREATE TABLE lbcat.authors_data AS SELECT DISTINCT email AS authors_email FROM lbcat.authors WHERE email IS NOT NULL
--ALTER TABLE lbcat.authors_data MODIFY authors_email VARCHAR(100) COLLATE utf8_unicode_ci NOT NULL
--ALTER TABLE lbcat.authors_data ADD PRIMARY KEY (authors_email)
--ALTER TABLE lbcat.authors ADD CONSTRAINT FK_AUTHORS_AUTHORS_DATA FOREIGN KEY (email) REFERENCES lbcat.authors_data (authors_email)
