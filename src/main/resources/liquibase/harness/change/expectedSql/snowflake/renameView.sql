CREATE VIEW ${CATALOG_NAME}.PUBLIC.test_view AS select id, first_name, last_name, email from authors
ALTER VIEW ${CATALOG_NAME}.PUBLIC.test_view RENAME TO ${CATALOG_NAME}.PUBLIC.test_view_new