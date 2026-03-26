CREATE TABLE testdb:informix.autoincrement_test (intColumn INT NOT NULL, dateColumn date, PRIMARY KEY (intColumn))
ALTER TABLE testdb:informix.autoincrement_test MODIFY intColumn SERIAL