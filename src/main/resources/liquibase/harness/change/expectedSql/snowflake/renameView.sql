CREATE VIEW "PUBLIC".test_view AS select id, first_name, last_name, email from authors
ALTER VIEW "PUBLIC".test_view RENAME TO "PUBLIC".test_view_new