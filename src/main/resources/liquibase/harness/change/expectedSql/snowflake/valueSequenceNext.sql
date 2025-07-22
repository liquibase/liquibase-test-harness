CREATE SEQUENCE TESTHARNESS.value_test_sequence START WITH 30 INCREMENT BY 2;
UPDATE LTHDB.TESTHARNESS.valueSequenceNextTable SET test_id = TESTHARNESS.value_test_sequence.nextval;