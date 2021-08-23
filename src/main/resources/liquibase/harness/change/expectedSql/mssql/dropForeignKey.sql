USE lbcat
GO
UPDATE DATABASECHANGELOGLOCK SET LOCKED = 1, LOCKEDBY = '5CD90139BV (192.168.192.1)', LOCKGRANTED = '2021-08-23T16:40:23.813' WHERE ID = 1 AND LOCKED = 0
GO
ALTER TABLE posts ADD CONSTRAINT fk_posts_authors_test FOREIGN KEY (id) REFERENCES authors (id) ON DELETE CASCADE
GO
ALTER TABLE posts DROP CONSTRAINT fk_posts_authors_test
GO
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE, DEPLOYMENT_ID) VALUES ('1', 'kristyl', 'liquibase/harness/change/changelogs/mssql/dropForeignKey.xml', GETDATE(), 1, '8:9980822ca262aa1a171d93c49c0e84d4', 'addForeignKeyConstraint baseTableName=posts, constraintName=fk_posts_authors_test, referencedTableName=authors dropForeignKeyConstraint baseTableName=posts, constraintName=fk_posts_authors_test', '', 'EXECUTED', NULL, NULL, '4.4.2', '9726024867')
GO
UPDATE DATABASECHANGELOGLOCK SET LOCKED = 0, LOCKEDBY = NULL, LOCKGRANTED = NULL WHERE ID = 1
GO