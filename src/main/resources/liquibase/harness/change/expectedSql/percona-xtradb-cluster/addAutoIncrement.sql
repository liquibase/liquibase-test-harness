CREATE TABLE lbcat.autoincrement_test (intColumn INT NOT NULL, dateColumn date NULL, CONSTRAINT PK_AUTOINCREMENT_TEST PRIMARY KEY (intColumn))
ALTER TABLE lbcat.autoincrement_test MODIFY intColumn INT AUTO_INCREMENT
ALTER TABLE lbcat.autoincrement_test AUTO_INCREMENT=100