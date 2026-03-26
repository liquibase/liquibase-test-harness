CREATE VIEW  testdb:informix.test_view AS SELECT * FROM (select id, first_name, last_name, email from authors) AS v
DROP VIEW testdb:informix.test_view