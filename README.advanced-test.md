## Advanced test

* The test behavior is as follows:
    * The test reads sql script provided in `src/main/resources/liquibase/harness/compatibility/advanced/initSql/primary` folder
    * `liquibase executeSql` is then executed to deploy script to the primary DB instance
    * The test takes a snapshot of the database after deployment by running Liquibase 'snapshot' command
    * Actual DB snapshot is compared to expected DB snapshot (provided in `src/main/resources/liquibase/harness/compatibility/advanced//expectedSnapshot`)
    * If snapshot is correct, the test then runs `liquibase generateChangelog` for all supported changeset formats (xml, sql, yml, json)
    * For all modeled changelog formats the test verifies generated changelog contains required changeset, that should be the same as initSql file name (if initSql was `createTable.sql`, the test will search for createTable changeset)
    * For sql format the test compares query from generated changelog with expected query (provided in `src/main/resources/liquibase/harness/compatibility/advanced/expectedSql/generateChangelog`)
    * If validation was successful, the test then runs `liquibase updateSql` on generated changelog to generate query
    * Compares generated query with expected query (provided in `src/main/resources/liquibase/harness/compatibility/advanced/expectedSql/generateChangelog`)
    * If expectedSql is not provided, validation is skipped and auto-generated sql script is saved to `src/test/resources/liquibase/harness/compatibility/advanced/expectedSql/generateChangelog`
    * The test runs `liquibase diff` command referencing secondary DB instance
    * Compares generated diff with expected diff, provided in `src/main/resources/liquibase/harness/compatibility/advanced/expectedDiff`
    * If generated diff is correct, the test then runs `liquibase update` to deploy generated changelog to secondary DB instance
    * The test runs `liquibase diff` command again
    * Compares generated diff with expected diff, provided in `src/main/resources/liquibase/harness/compatibility/advanced/expectedDiff/empty.txt` to verify no differences between two instances
    * Secondary instance is then cleared by running `liquibase dropAll` command
    * The test then runs `liquibase executeSql` with sql script, provided in `src/main/resources/liquibase/harness/compatibility/advanced/initSql/secondary` folder
    * The test then runs `liquibase diffChangelog` for all supported changeset formats (xml, sql, yml, json) referencing secondary instance
    * For all modeled changelog formats the test verifies generated changelog contains required changeset, that should be the same as initial changelog name, as well as reversed changeset (if initial changelog was `createTable.xml`, the test will search for createTable and dropTable changesets)
    * For sql format the test compares query from generated changelog with expected query (provided in `src/main/resources/liquibase/harness/compatibility/advanced/expectedSql/diffChangelog`)
    * If validation was successful, the test then runs `liquibase updateSql` on generated changelog to generate query
    * Compares generated query with expected query (provided in `src/main/resources/liquibase/harness/compatibility/advanced/expectedSql/diffChangelog`)
    * If expectedSql is not provided, validation is skipped and auto-generated sql script is saved to `src/test/resources/liquibase/harness/compatibility/advanced/expectedSql/diffChangelog`
    * If query is correct, the test then runs `liquibase update` to deploy diff changelog to secondary DB instance
    * The test runs `liquibase diff` command referencing secondary DB instance
    * Compares generated diff with expected diff, provided in `src/main/resources/liquibase/harness/compatibility/advanced/expectedDiff/empty.txt` to verify no differences between two instances
    * Both DB instances are then cleared by running `liquibase dropAll`
    * Finally, the test deletes all generated changelogs from `src/test/resources/liquibase/harness/compatibility/advanced/` folder