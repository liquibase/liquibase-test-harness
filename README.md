# A Harness of Integration Tests

## Framework

The test harness consists of a variety of standard tests to ensure the database-specific interactions within Liquibase work against specific 
versions and configurations

The test harness logically consists of three parts: 
1. Test logic
1. Configuration files containing inputs for the test logic
1. Configuration files containing outputs/expectations for the test logic. 

The built-in tests are designed to test an overall functional flow, iterating over all configured connections. 
For each connection, it will run each applicable input configuration and compare it to the expected output/expectations.

Both the input and output configuration files can be defined in a way that makes them apply to all databases or to specific types and/or specific versions.

The general pattern is that for each directory containing configuration files:
- Files directly in that root apply to all databases. Example: `liquibase/harness/change/changelogs`
- Files in a subdirectory named for the database type apply to only that type of database. Example: `liquibase/harness/change/changelogs/mysql`
- Files in a subdirectory with a version apply only to this version of the database. Example: `liquibase/harness/change/changelogs/mysql/8`

At each level in that hierarchy, new configurations can be added and/or can override configurations from a lower level. 

Currently, there is one test type defined in the test harness:
1. Change Object Tests     

This repository is configured to run against databases supported by Liquibase Core. 

Extensions that add support for additional databases and/or define additional functionality can add this framework as a dependency and use the existing tests to:
- More easily verify their new functionality works
- And that it also doesn't break existing logic

## Configuring Execution   

#### Configuration File

The test harness will look for a file called `harness-config.yml` in the root of your classpath.

That file contains a list of the database connections to test against, as well as an ability to control which subsets of tests to run.   

See `src/test/resources/harness-config.yml` to see what this repository is configured to use.

## For use in extensions

For more information on using the test harness in your extension, see [README.extensions.md] 

# Framework Tests
 
## Change Objects Test

The `groovy/liquibase/harness/ChangeObjectsTests.groovy` test executes changelogs against the database and validates the SQL generated by them as well as 
whether they make the expected changes.

* The test behavior is as follows:
  * It reads the changesets from the changelogs provided in `src/main/resources/liquibase/harness/change/changelogs` folders (recursively)
  * Runs the changeset thru the SqlGeneratorFactory to generate SQL
  * Compares the generated SQL with the expected SQL (provided in `src/main/resources/liquibase/harness/change/expectedSql`)
  * If the SQL generation is correct, the test then runs `liquibase update` to deploy the
  changeset to the DB
  * The test takes a snapshot of the database after deployment
  * The deployed changes are then rolled back 
  * Finally, the actual DB snapshot is compared to the expected DB snapshot (provided in `src/main/resources/liquibase/harness/change/expectedSnapshot`)

#### Types of input files
* The tests work with the 4 types of input files that are supported by liquibase itself - xml, yaml, json, sql.
Thus files with extensions 'xml', 'sql', 'json', 'yml', 'yaml' are taken into account, but not all formats together in the same run.
* The default format is xml, so by default only changelogs with xml file extension are executed.
To change it to another format, like 'sql' for instance, specify `-DinputFormat=sql` as the command line argument for Maven or as VM option to your JUnit test run config.


### Adding a change object test
1) Go to `src/main/resources/liquibase/harness/change/changelogs` and add the xml (or other) changeset for the change type you
 want to test.
  - The framework tries to rollback changes after deploying them to DB. If liquibase knows how to do a rollback for that particular changeset, it will automatically do that.
If not, you will need to provide the rollback by yourself. To learn more about rollbacks read [Rolling back changesets](https://docs.liquibase.com/workflows/liquibase-community/using-rollback.html) article.
2) Go to `src/main/resources/liquibase/harness/change/expectedSql` and add the expected generated SQL. 
 - You will need to add this under the database specific folder.
 - NOTE: If your changeset will generate multiple SQL statements, you should add each SQL statement as a separate line. (See `renameTable.sql` in the postgres folder for an example.)
 - If you would like to test another DB type, please add the requisite folder.
3) Go to `src/main/resources/liquibase/harness/change/expectedSnapshot` and add the expected DB Snapshot results.
  - To verify the absence of an object in a snapshot (such as with drop* commands) add `"_noMatch": true,` to that tree level where the missing object should be verified.
  See [dropSequence.json](src/test/resources/expectedSnapshot/postgresql/dropSequence.json) as an example.
  - You will need to add this under the database specific folder.
  - If you would like to test another DB type, please add the requisite folder.
4) Go to your IDE and run the test class `ChangeObjectTests.groovy` (You can also choose to run `BaseTestHarnessSuite`, or `LiquibaseHarnessSuiteTest` -- at present they all work the same).

# Running the Tests

### Minimum Requirements
Java 1.8

1) Make sure you have a docker container up and running first
2) Go to `src/test/resources/docker` and run `docker-compose up -d`. 
Wait until the databases start up.
3) Open `src/test/groovy/liquibase/harness/LiquibaseHarnessSuiteTest.groovy` in your IDE of choice and run it

## Running from the cmd line with Maven
Execute `mvn test` with the (optional) flags outlined below:
* `-DinputFormat=xml` or select from the other inputFormats listed in [Types of input files](#types-of-input-files)
* `-DchangeObjects=createTable,dropTable` flag allows you to run specific changeObjects rather than all. Use comma
 separated lists.
* `-DdbName=mysql` overrides the database type. This is only a single value property for now.
* `-DdbVersion` overrides the database version. Works in conjunction with `-DdbName` flag.

# Cleanup

When you are done with test execution, run `docker-compose down --volumes` to stop the docker containers 
gracefully and to allow the tests to start from a clean slate on the next run.

#### Stay tuned, there is more to come!
