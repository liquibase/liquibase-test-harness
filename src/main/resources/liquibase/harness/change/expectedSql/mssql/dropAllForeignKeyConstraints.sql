USE lbcat
GO
UPDATE DATABASECHANGELOGLOCK SET LOCKED = 1, LOCKEDBY = '5CD90139BV (192.168.192.1)', LOCKGRANTED = '2021-08-23T16:40:17.234' WHERE ID = 1 AND LOCKED = 0
GO
ALTER TABLE posts ADD CONSTRAINT fk_posts_authors_test_1 FOREIGN KEY (author_id) REFERENCES authors (id)
GO
ALTER TABLE posts ADD CONSTRAINT fk_posts_authors_test_2 FOREIGN KEY (id) REFERENCES authors (id)
GO
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE, DEPLOYMENT_ID) VALUES ('1', 'as', 'liquibase/harness/change/changelogs/dropAllForeignKeyConstraints.xml', GETDATE(), 1, '8:bc2f92e2febe54a0db75a6260ba515e6', 'addForeignKeyConstraint baseTableName=posts, constraintName=fk_posts_authors_test_1, referencedTableName=authors addForeignKeyConstraint baseTableName=posts, constraintName=fk_posts_authors_test_2, referencedTableName=authors', '', 'EXECUTED', NULL, NULL, '4.4.2', '9726018111')
GO
INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, MD5SUM, DESCRIPTION, COMMENTS, EXECTYPE, CONTEXTS, LABELS, LIQUIBASE, DEPLOYMENT_ID) VALUES ('2', 'as', 'liquibase/harness/change/changelogs/dropAllForeignKeyConstraints.xml', GETDATE(), 2, '8:69de0b17832094aad134b2026b512ac3', 'dropAllForeignKeyConstraints baseTableName=posts', '', 'EXECUTED', NULL, NULL, '4.4.2', '9726018111')
GO
UPDATE DATABASECHANGELOGLOCK SET LOCKED = 0, LOCKEDBY = NULL, LOCKGRANTED = NULL WHERE ID = 1
GO