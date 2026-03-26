ALTER TABLE testdb:informix.authors ADD booleanColumn BOOLEAN
ALTER TABLE testdb:informix.authors MODIFY (booleanColumn BOOLEAN DEFAULT 't')