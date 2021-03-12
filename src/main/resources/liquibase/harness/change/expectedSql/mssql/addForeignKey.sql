CREATE TABLE lbcat2.countries (id int NOT NULL, name varchar(50), CONSTRAINT PK_COUNTRIES PRIMARY KEY (id))
CREATE TABLE person (id int IDENTITY (1, 1) NOT NULL, CONSTRAINT PK_PERSON PRIMARY KEY (id))
ALTER TABLE person ADD country_id int
ALTER TABLE person ADD CONSTRAINT fk_person_country FOREIGN KEY (country_id) REFERENCES lbcat2.countries (id)