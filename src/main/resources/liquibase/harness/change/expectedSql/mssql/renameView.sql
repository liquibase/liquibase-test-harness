CREATE VIEW test_view AS select id, first_name, last_name, email from authors
exec sp_rename 'test_view', 'test_view_new'