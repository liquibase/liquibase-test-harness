ALTER TABLE testdb:informix.authors ADD numericColumn INT
ALTER TABLE testdb:informix.authors MODIFY (numericColumn INT DEFAULT 100000000)