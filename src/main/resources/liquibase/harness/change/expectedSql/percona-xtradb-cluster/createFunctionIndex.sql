CREATE TABLE lbcat.lb2160 (id INT NOT NULL, first_name VARCHAR(50) NULL, last_name VARCHAR(50) NULL, CONSTRAINT PK_LB2160 PRIMARY KEY (id))
CREATE INDEX idx_lb2160 ON lbcat.lb2160((lower(first_name)))