CREATE VIEW LTHDB.PUBLIC.test_view AS select id, first_name, last_name, email from authors
ALTER VIEW LTHDB.PUBLIC.test_view RENAME TO LTHDB.PUBLIC.test_view_new