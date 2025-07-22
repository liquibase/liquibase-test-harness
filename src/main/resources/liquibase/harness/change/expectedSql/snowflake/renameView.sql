CREATE VIEW LTHDB.TESTHARNESS.test_view AS select id, first_name, last_name, email from authors
ALTER VIEW LTHDB.TESTHARNESS.test_view RENAME TO LTHDB.TESTHARNESS.test_view_new