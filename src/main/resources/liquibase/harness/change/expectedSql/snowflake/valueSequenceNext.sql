CREATE TABLE LTHDB."PUBLIC".valueSequenceNextTable (test_id INT, test_column VARCHAR(50));
CREATE SEQUENCE "PUBLIC".test_sequence START WITH 30 INCREMENT BY 2;
UPDATE LTHDB."PUBLIC".valueSequenceNextTable SET test_id = "PUBLIC".test_sequence.nextval;