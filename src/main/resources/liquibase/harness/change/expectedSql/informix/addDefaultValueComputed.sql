INVALID TEST

-- Bug on Liquibase side: incorrect generated sql
-- Actual:
-- ALTER TABLE testdb:informix.posts
--     MODIFY (inserted_date date DEFAULT DATETIME YEAR TO FRACTION(5) DEFAULT CURRENT YEAR TO FRACTION(5));
-- Expected:
-- ALTER TABLE testdb:informix.posts
--     MODIFY (inserted_date DATETIME YEAR TO FRACTION(5) DEFAULT CURRENT YEAR TO FRACTION(5));