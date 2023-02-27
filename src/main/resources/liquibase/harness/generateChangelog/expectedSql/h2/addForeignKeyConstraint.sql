CREATE TABLE TEST_TABLE_BASE (ID INT NOT NULL, CONSTRAINT PK_TEST_TABLE_BASE PRIMARY KEY (ID));

CREATE TABLE TEST_TABLE_REFERENCE (ID INT, TEST_COLUMN INT NOT NULL, CONSTRAINT PK_TEST_TABLE_REFERENCE PRIMARY KEY (TEST_COLUMN));

ALTER TABLE TEST_TABLE_BASE ADD CONSTRAINT TEST_FK FOREIGN KEY (ID) REFERENCES TEST_TABLE_REFERENCE (TEST_COLUMN) ON UPDATE RESTRICT ON DELETE CASCADE;
