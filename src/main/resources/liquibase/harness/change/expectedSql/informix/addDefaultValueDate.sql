INVALID TEST

-- Bug on Liquibase side: Incorrect SQL generated on rollback defaultValueDate DAT-19231
        --[Failed SQL: (-201) ALTER TABLE testdb:informix.authors MODIFY (dateTimeColumn datetime)]