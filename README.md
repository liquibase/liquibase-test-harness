# A Harness of Integration Tests
## Framework
The Harness Test framework logically consists of 2 main folders:
1) A src/test/groovy/liquibase/harness with groovy code and
2) A src/test/resources with test resources

## Test Scope
#### Change Object Test
* At present there is only one test class to execute, which is `groovy/liquibase/harness/ChangeObjectsTest.groovy` -- This test class
will execute a set of test cases based on provided input. 
* The test input is configured via `resources/testConfig.yml` -- This yaml file takes 
input changelogs from the `resources/changelogs` folder 
* The test behavior is as follows:
  * It reads the changesets from the changelogs provided
  * Runs the changeset thru the SqlGeneratorFactory to generate SQL
  * Compares the generated SQL with the expected SQL (provided in `resources/expectedSql`)
  * If the SQL generation is correct, the test then runs `liquibase update` to deploy the
  changeset to the DB
  * The test takes a snapshot of the database after deployment
  * The deployed changes are then rolled back 
  * Finally, the actual DB snapshot is compared to the expected DB snapshot (provided in `resources/expectedSnapshot`)

#### Types of input files
* The tests work with the 4 types of input files that are supported by liquibase itself - xml, yaml, json, sql.
Thus files with extensions 'xml', 'sql', 'json', 'yml', 'yaml' are taken into account, but not all together.
* The default format is xml, so by default only changelogs with xml file extension are executed.
To change it to another format, like 'sql' for instance, specify `-DinputFormat=sql` as the command line argument for Maven or as VM option to your JUnit test run. config.


### Adding a change object test
1) Go to `src/test/resources/changelogs` and add the xml changeset for the change type you want to test.
  - The framework tries to rollback changes after deploying them to DB. If liquibase know how to do a rollback for that particular change, it will automatically do that.
If not, you have to provide the rollback by yourself. To learn more about rollbacks read [Rolling back changesets](https://docs.liquibase.com/workflows/liquibase-community/using-rollback.html) article.
2) Go to `src/test/resources/expectedSQL` and add the expected generated SQL. 
You will need to add this under the database specific folder. Currently we only have Postgresql & MySQL folders. 
NOTE: If your changeset will generate multiple SQL statements, you should add each SQL statement as a separate line. See renameTable.sql in the postgres folder for an example.
If you would like to test another DB type, please add the requisite folder.
3) Go to `src/test/resources/expectedSnapshot` and add the expected DB Snapshot results.
  - To verify absence of an object in snapshot (such as drop* commands) add `"_noMatch": true,` to that tree level where missing object should be verified.
  See [dropSequence.json](src/test/resources/expectedSnapshot/postgresql/dropSequence.json) as an example.
  - You will need to add this under the database specific folder. Currently we only have Postgresql & MySQL folders. 
  - If you would like to test another DB type, please add the requisite folder.
4) Go to your IDE and run the test class `ChangeObjectsTest.groovy`

## Running the integration test suite is as easy as one-two-three
1) Make sure you have docker container up and running first
2) Go to  `src/test/resources/docker` and run `docker-compose up -d`. 
Wait until the databases start up.
3) Open `src/test/groovy/liquibase/harness/ChangeObjectsTest.groovy` in your IDE of choice 
and run the test class `ChangeObjectsTest.groovy`

## Cleanup
When you are done with test execution, run `docker-compose down --volumes` to stop the docker containers 
gracefully and to allow the tests to start from a clean slate on the next run.

## Running from cmd
Execute `mvn integration-test` with next flags 
* `-DinputFormat=xml` or other inputFormat among listed in [Types of input files](#types-of-input-files)
* `-DchangeObjects=createTable,dropTable` flag allows to override changeObjects configured in testConfig.yml. Comma
 separated list is expected.
* `-DdbName=mysql` overrides database to be used in this run. Single value property as for now.
* `-DdbVersion` overrides database version to be used in this run, works only in conjunction with `dbName` flag
#### Stay tuned, is more to come!
