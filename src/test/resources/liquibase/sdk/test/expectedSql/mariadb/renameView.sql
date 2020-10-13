CREATE VIEW lbcat.test_view AS select id, first_name, last_name, email from authors
RENAME TABLE lbcat.test_view TO lbcat.test_view_new