# A Harness of Integration Tests
## Framework
The Harness Test framework logically consists of 2 main folders:
1) A src/main/groovy/liquibase/harness with groovy code and
2) A src/main/resources with test resources

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
  * Finally, the actual DB snapshot is compared to the expected DB snapshot (provided in `resources/expectedSnapshot`)

#### Adding a change object test
1) Go to `src/main/resources/changelogs` and add the xml changeset for the change type you want to test.
2) Go to `src/main/resources/expectedSQL` and add the expected generated SQL. 
You will need to add this under the database specific folder. Currently we only have Postgresql & MySQL folders. 
If you would like to test another DB type, please add the requisite folder.
3) Go to `src/main/resources/expectedSnapshot` and add the expected DB Snapshot results. 
You will need to add this under the database specific folder. Currently we only have Postgresql & MySQL folders. 
If you would like to test another DB type, please add the requisite folder.
4) Go to your IDE and run the test class `ChangeObjectsTest.groovy`

## Running the integration test suite is as easy as one-two-three
1) Make sure you have docker container up and running first
2) Go to  `src/main/resources/docker` and run `docker-compose up -d`. 
Wait until the databases start up.
3) Open `src/main/groovy/liquibase/harness/ChangeObjectsTest.groovy` in your IDE of choice 
and run the test class `ChangeObjectsTest.groovy`

## Cleanup
When you are done with test execution, run `docker-compose down --volumes` to stop the docker containers 
gracefully and to allow the tests to start from a clean slate on the next run.

P.S. - Please note that the Maven surefire plugin isn't configured to run the harness tests yet. This is in the works.



#### Stay tuned, is more to come!