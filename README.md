# A Harness of Integration Tests

[![Default Test Execution](https://github.com/liquibase/liquibase-test-harness/actions/workflows/main.yml/badge.svg)](https://github.com/liquibase/liquibase-test-harness/actions/workflows/main.yml) [![Advanced Test Execution](https://github.com/liquibase/liquibase-test-harness/actions/workflows/advanced.yml/badge.svg)](https://github.com/liquibase/liquibase-test-harness/actions/workflows/advanced.yml) [![Oracle Parallel Test Execution](https://github.com/liquibase/liquibase-test-harness/actions/workflows/OracleRunParallel.yml/badge.svg)](https://github.com/liquibase/liquibase-test-harness/actions/workflows/OracleRunParallel.yml) [![AWS Cloud Database Test Execution](https://github.com/liquibase/liquibase-test-harness/actions/workflows/aws.yml/badge.svg)](https://github.com/liquibase/liquibase-test-harness/actions/workflows/aws.yml) [![Azure Cloud Sql DB Test](https://github.com/liquibase/liquibase-test-harness/actions/workflows/azure.yml/badge.svg)](https://github.com/liquibase/liquibase-test-harness/actions/workflows/azure.yml) [![Google Cloud Database Test Execution](https://github.com/liquibase/liquibase-test-harness/actions/workflows/gcp.yml/badge.svg)](https://github.com/liquibase/liquibase-test-harness/actions/workflows/gcp.yml) [![Oracle OCI Test Execution](https://github.com/liquibase/liquibase-test-harness/actions/workflows/oracle-oci.yml/badge.svg)](https://github.com/liquibase/liquibase-test-harness/actions/workflows/oracle-oci.yml) [![Snowflake Cloud](https://github.com/liquibase/liquibase-test-harness/actions/workflows/snowflake.yml/badge.svg)](https://github.com/liquibase/liquibase-test-harness/actions/workflows/snowflake.yml) 

## Test-Harness Support Matrix

| Database                  | Versions Tested                       | Verification Level             |
|---------------------------|---------------------------------------|--------------------------------|
| Aurora MySQL              | `8`                                   | Advanced                       | 
| Aurora Postgres           | `14, 16`                              | Advanced                       |
| AWS Postgres RDS          | `12, 13, 14, 16`                      | Advanced                       |
| AWS Oracle RDS            | `19.0`                                | Advanced                       |
| AWS MySQL                 | `8`                                   | Advanced                       |
| AWS MariaDB               | `10.6`                                | Advanced                       |
| AWS SQL Server            | `2019`                                | Advanced                       |
| Azure SQL DB              | `latest`                              | Advanced                       |
| Azure Mysql DB            | `5.7`                                 | Advanced                       |
| Azure SQL MI              | `latest`                              | BaseHarnessSuite               |
| Azure PostgreSQL FlS      | `14, 15, 16`                          | Advanced                       |
| GCP PostgreSQL            | `12, 13, 14`                          | Advanced                       |
| GCP MySQL                 | `8`                                   | Advanced                       |
| GCP SQL Server            | `2019`                                | Advanced                       |
| MariaDB                   | `10.2, 10.3 , 10.4, 10.5, 10.6, 10.7` | Advanced                       |
| Postgres                  | `12, 13, 14, 15, 16`                  | Advanced                       |
| MySQL                     | `5.6, 5.7, 8`                         | Advanced                       |
| SQL Server                | `2017`, `2019`, `2022`                | Advanced                       |
| Percona XtraDB            | `5.7`, `8.0`                          | Advanced                       |
| Oracle                    | `18.3.0, 18.4.0, 21.3.0`              | Advanced                       |
| CockroachDB               | `23.1, 23.2, 24.1`                    | Advanced                       |
| EDB                       | `12, 13, 14, 15, 16`                  | Advanced                       |
| DB2 on z/OS               | `11.1, 12`                            | BaseHarnessSuite               |
| DB2 on Linux/Unix/Windows | `11.5.7`                              | Advanced                       |
| H2                        | `2.2.220`                             | Advanced                       |
| SQLite                    | `3.34.0`                              | Advanced                       |
| Apache Derby              | `10.14.2.0`                           | Advanced                       |
| Firebird                  | `3.0, 4.0`                            | Advanced                       |
| HSQLDB                    | `2.5, 2.6, 2.7`                       | Advanced                       |
| Snowflake                 | `latest`                              | BaseHarnessSuite               |

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
##### Note: The version folder name should match exactly with the DB version provided in `harness-config.yml` file. We do not split this to major/minor/patch subversion folders currently.

At each level in that hierarchy, new configurations can be added and/or can override configurations from a lower level. 

Currently, there are six test types defined in the test harness:
* Foundational test
* Advanced test
* GenerateChangelog Command test
* Change Object Tests
* Change Data Tests
* Snapshot Command Test
* Diff Command Test

Currently, there are three test suites in the test harness:
* BaseHarnessSuite, contains:
    * ChangeObjectTest
    * ChangeDataTest
* FoundationalHarnessSuite, contains:
    * FoundationalTest
* AdvancedHarnessSuite, contains:
    * AdvancedTest

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

## FoundationalTest

This test validates work of basic Liquibase functions. 
1) runs Liquibase validate command to ensure the changelog is valid;
2) runs changelog (all supported formats: XML, YAML, JSON, SQL) with basic metadata decorations (labels, contexts, comments) using Liquibase update command;
3) runs Liquibase tag command;
4) runs select query from DATABASECHANGELOG table using jdbc to ensure contexts, labels, comments and tags are present in metadata;
5) runs verification query using jdbc to ensure a test object was actually created or modified during Liquibase update command
by comparing it to JSON-formatted expected result set (**Note! Result set for your database may differ from existing result set if
it is not present in test**)
6) runs Liquibase history command;
7) runs Liquibase status command;
8) runs Liquibase rollback command;
9) runs verification query to ensure a test object was actually removed during Liquibase rollback command;

### Running FoundationalTest against your database
As far as this test validates work of basic Liquibase functions it is essential to keep its configuration as simple as possible:
1. If you have your database instance up and running you need to just add appropriate configuration details to `src/test/resources/harness-config.yml` file.
Following the example:
   - **name**: `database_name` (**mandatory**)  - is used in test input files structure to override default files </br>
     **version**: `database_version` (optional) </br>
     **prefix**: `local` (optional parameter required for CI/CD tests, leave it empty or set `local`) </br>
     **url**: `db_connection_url` (**mandatory**) </br>
     **username**: `username` (optional if your database authentication config doesn't require it) </br>
     **password**: `password` (optional if your database authentication config doesn't require it) </br>
2. Add driver dependency for you database to POM.xml file

3. To run the test go to you IDE run configurations and add new JUnit configuration. Add 
`liquibase.harness.compatibility.foundational.FoundationalTest` as target class and use -DdbName, -DdbVersion to set up
appropriate parameters. Or you may just comment out/delete all existing configurations in harness-config.yml
file leaving just your configuration and run FoundationalTest directly from the class file. 

In case you want to set up your database instance using docker image then you may use 
`src/test/resources/docker/docker-compose.yml` file for configuration.

## Advanced test

The `groovy/liquibase/harness/compatibility/advanced/AdvancedTest.groovy` test validates Liquibase `snapshot`, `generateChangelog`, `diffChangelog` and `diff` commands.

### Configuring Advanced test
1) Go to `src/main/resources/liquibase/harness/compatibility/advanced/initSql/primary` and add sql script for the change type you want to test.
- Use change type as file name (createTable.sql, addCheckConstraint.sql, etc.) as the test will use it for generated changelog validation.
- Some change types (addColumn, addPrimaryKey, addUniqueConstraint) do not always produce separate changesets during generateChangelog/diffChangelog execution, so use column.sql, primary.sql, unique.sql for initSql file name instead.
2) Go to `src/main/resources/liquibase/harness/compatibility/advanced/initSql/secondary` and add sql script to setup secondary DB instance for diffChangelog command verification.
- Configure this script to contain the change type under test, one that will differ from the one in initial changelog.
3) Go to `src/main/resources/liquibase/harness/compatibility/advanced/expectedSql/generateChangelog` and add the sql script you expect liquibase to generate during updateSql command execution for generated changelog.
- If expectedSql is not provided, the test will auto-generate one in the `src/test/resources/liquibase/harness/compatibility/advanced/expectedSql/generateChangelog` folder. Please verify its content and use it as expectedSql test data.
4) Go to `src/main/resources/liquibase/harness/compatibility/advanced/expectedSql/diffChangelog` and add the sql query you expect liquibase to generate during updateSql command execution for generated diff changelog.
- If expectedSql is not provided, the test will auto-generate one in the `src/test/resources/liquibase/harness/compatibility/advanced/expectedSql/diffChangelog` folder. Please verify its content and use it as expectedSql test data.
5) Go to `src/main/resources/liquibase/harness/compatibility/advanced/expectedSnapshot` and add expected DB Snapshot results.
- See [example.json](src/main/resources/liquibase/harness/compatibility/advanced/expectedSnapshot/example.json) as an example.
  This file contains snapshots for table, column, check constraint & function db objects. Use their structure as an example for your own db objects.
- To verify the absence of an object in a snapshot (such as with drop* commands) add `"_noMatch": true,` in the applicable tree level where the missing object should be verified.
  See [dropSequence.json](src/main/resources/liquibase/harness/change/expectedSnapshot/postgresql/dropSequence.json) as an example.
  Additionally, the `_noMatchField` parameter can be used to define the exact property which should be absent or different for that particular database object (for example Column, Table etc.)
  see [createTableWithNumericColumn.json](src/main/resources/liquibase/harness/change/expectedSnapshot/postgresql/createTableWithNumericColumn.json)
6) Go to `src/main/resources/liquibase/harness/compatibility/advanced/expectedDiff` and add expected diff results.
- See [example.txt](src/main/resources/liquibase/harness/compatibility/advanced/expectedDiff/example.txt) as an example.
  This file contains example of missing and unexpected objects representation in typical diff file.

- NOTE: All test data should be added under the database specific folder. If you would like to test another DB type, please add the requisite folder.
- More information on Advanced test behavior can be found in README.advanced-test.md

### Running AdvancedTest against your database
1. If you have your database instance up and running you need to just add appropriate configuration details to `src/test/resources/harness-config.yml` file.
   Following the example:
    - **name**: `database_name` (**mandatory**) </br>
      **version**: `database_version` (optional) </br>
      **prefix**: `local` (optional parameter required for CI/CD tests, leave it empty or set `local`) </br>
      **url**: `db_connection_url` (**mandatory**) </br>
      **username**: `username` (optional if your database authentication config doesn't require it) </br>
      **password**: `password` (optional if your database authentication config doesn't require it) </br>
2. Add driver dependency for you database to POM.xml file
3. Make sure, your database has two instances. Please name your secondary instance as `secondarydb`
4. To run the test go to you IDE run configurations and add new JUnit configuration. Add
   `liquibase.harness.compatibility.advanced.AdvancedTest` as target class and use -DdbName, -DdbVersion to set up
   appropriate parameters. Or you may just comment out/delete all existing configurations in harness-config.yml
   file leaving just your configuration and run FoundationalTest directly from the class file.
5. In case you want to set up your database instance using docker image then you may use
`src/test/resources/docker/docker-compose.yml` file for configuration.

### GenerateChangelogTest

* This test validates work of generateChangelog command.

* The test behavior is as follows:
    * It reads the changesets from the changelogs provided in `src/main/resources/liquibase/harness/generateChangelog/expectedChangeLog` folders (recursively)
    * Runs Liquibase 'update' command to create objects on database
    * Runs Liquibase 'generateChangelog' command to generate changelog (all supported formats: XML, YAML, JSON, SQL)
    * Validates if generated changelogs contain changeset corresponding name for XML, YAML, JSON formats or if generated query is correct for SQL format
    * Finally, deployed changes are then rolled back
    
### DiffCommandTest

This test executes the following steps:
* Reads `src/test/resources/harness-config.yml` and `src/main/resources/liquibase/harness/diff/diffDatabases.yml` to locate the
  databases that need to be compared
* Creates a diff based on 2 databases (targetDatabase and referenceDatabase) from `diffDatabases.yml`
* Generates the changelog based on diff
* Applies the generated changelog to the targetDatabase
* Checks the diff between the target and reference databases again
* If some diffs still exist, then they are matched with the expected diff from `liquibase/harness/diff/expectedDiff` folder

#### Warning: This is a destructive test -- it will alter the state of targetDatabase to match the referenceDatabase.

### SnapshotCommandTests

This test validates work of Liquibase 'snapshot' command by comparing expected and generated snapshots
after a DB object was created.

## Change Objects Test

The test-harness validates most of the Data Definition Language related Change Types as listed on [Home Page](https://docs.liquibase.com/change-types/home.html). 
The primary focus is on add, create, drop & rename database objects.

The `groovy/liquibase/harness/ChangeObjectsTests.groovy` test executes changelogs against the database and validates the SQL generated by them as well as 
whether they make the expected changes.

* The test behavior is as follows:
  * It reads the changesets from the changelogs provided in `src/main/resources/liquibase/harness/change/changelogs` folders (recursively)
  * Runs Liquibase 'updateSql' command to generate query
  * Compares generated query with expected query (provided in `src/main/resources/liquibase/harness/change/expectedSql`)
  * If the query generation is correct, the test then runs `liquibase update` to deploy the changeset to the DB
  * The test takes a snapshot of the database after deployment by running Liquibase 'snapshot' command
  * Actual DB snapshot is compared to expected DB snapshot (provided in `src/main/resources/liquibase/harness/change/expectedSnapshot`)
  * Finally, deployed changes are then rolled back by either using `rollbackToDate` (default) or `rollback` by tag (**test-harness-tag**). See `-DrollbackStrategy` option below for more information.


#### Types of input files
* The tests work with 4 types of input files that are supported by Liquibase itself - xml, yaml, json, sql.
Thus files with extensions 'xml', 'sql', 'json', 'yml', 'yaml' are taken into account, but not all formats together in the same run.
* The default format is xml, so by default only changelogs with xml file extension are executed.
To change it to another format, like 'sql' for instance, specify `-DinputFormat=sql` as the command line argument for Maven or as VM option to your JUnit test run config.


### Adding a change object test
1) Go to `src/main/resources/liquibase/harness/change/changelogs` and add the xml (or other) changeset for the change type you
 want to test.
  - The framework tries to rollback changes after deploying it to DB. If Liquibase knows how to do a rollback for that particular changeset, it will automatically do that.
If not, you will need to provide the rollback by yourself. To learn more about rollbacks read [Rolling back changesets](https://docs.liquibase.com/workflows/liquibase-community/using-rollback.html) article.
2) Go to `src/main/resources/liquibase/harness/change/expectedSql` and add expected query. 
 - You will need to add this under the database specific folder.
 - NOTE: If your changeSet will generate multiple SQL statements, you should add each SQL statement as a separate line. (See `renameTable.sql` in the postgres folder for an example.)
 - If you would like to test another DB type, please add the requisite folder.
3) Go to `src/main/resources/liquibase/harness/change/expectedSnapshot` and add expected DB Snapshot results.
  - To verify the absence of an object in a snapshot (such as with drop* commands) add `"_noMatch": true,` to that tree level where the missing object should be verified.
  See [dropSequence.json](src/main/resources/liquibase/harness/change/expectedSnapshot/postgresql/dropSequence.json) as an example. 
Additionally the `_noMatchField` parameter can be used to define the exact property which should be absent or different for that particular database object (for example Column, Table etc.) 
see [createTableWithNumericColumn.json](src/main/resources/liquibase/harness/change/expectedSnapshot/postgresql/createTableWithNumericColumn.json)
  - You will need to add this under the database specific folder.
  - If you would like to test another DB type, please add the requisite folder.
4) Go to your IDE and run the test class `ChangeObjectTests.groovy` (You can also choose to run `BaseTestHarnessSuite`, `AdvancedHarnessSuite`, or `FoundationalHarnessSuite`).

## Change Data Test

The primary goal of these tests is to validate change types related to DML (Data Manipulation Language) aspect.
Generally it is similar to ChangeObjectTests except it doesn't use Liquibase snapshot to verify data but obtains result set via JDBC.
 - `src/main/resources/liquibase/harness/data/changelogs` - add DML related changelogs here;
 - `src/main/resources/liquibase/harness/data/checkingSql` - add select query which will obtain a result set from required DB object;
 - `src/main/resources/liquibase/harness/data/expectedResultSet` - add JSON formatted expected result set from required DB object;
    where left part of a JSON node is the name of a change type and right part is JSON Array with a result set;
 - `src/main/resources/liquibase/harness/data/expectedSql` - add query which is expected to be generated by Liquibase;
 

## Minimum Requirements
 - Java 11. Java 8 should actually work for most of the platforms that don't have jdbc drivers that require Java 11, those that do are
Firebird, HyperSQL(HSQLDB), Microsoft SQL Server. Downgrade java and jdbc driver versions in pom at your own risk.
 - Maven >=3.5

## Running the Tests

1) Make sure you have a docker container up and running first
2) Go to `src/test/resources/docker` and run `docker compose up -d`. 
Wait until the databases start up.
3) Open `src/test/groovy/liquibase/harness/LiquibaseHarnessSuiteTest.groovy` in your IDE of choice and run it

## Running from the cmd line with Maven
Build the project first by running `mvn clean install -DskipTests` 

Execute `mvn test` with the (optional) flags outlined below:
* `-DinputFormat=xml` or select from the other inputFormats listed in [Types of input files](#types-of-input-files)
* `-DchangeObjects=createTable,dropTable` flag allows you to run specific changeObjects rather than all. Use comma
 separated lists.
* `-DchangeData=insert,delete` flag that allows to run specific changeData through ChangeDataTests. Use comma separated list
* `-Dchange=createTable,createView` flag that allows to run specific change type through AdvancedTest. Use comma separated list
* `-DconfigFile=customConfigFile.yml` enables to override default config file which is(`src/test/resources/harness-config.yml`)
* `-Dprefix=docker` filters database from config file by some common platform identifier. E.g. all AWS based platforms, all from default docker file.
* `-DdbName=mysql` overrides the database type. This is only a single value property for now.
* `-DdbVersion` overrides the database version. Works in conjunction with `-DdbName` flag.
* `-DdbUsername=myUsername` overrides the database login username. Providing placeholder username in config.yml file is still required.
* `-DdbPassword=myPassword` overrides the database login password. Providing placeholder password in config.yml file is still required.
* `-DdbUrl=myUrl` overrides the database url. Providing placeholder url in config.yml file is still required.
* `-DrollbackStrategy` overrides the default rollback strategy of `rollbackToDate` where we create a timestamp in UTC timezone and then try to rollback to that point in time. But this rollback strategy might not work well in some cases like cloud databases for instance -- cloud databases are often in different timezones than the test-harness runners, so the `rollback` command can be used instead in conjunction with the `test-harness-tag` tag. To do so, use `-DrollbackStrategy=rollbackByTag`.
* `-Dliquibase-core.version` for macOS and Linux, or `-D"liquibase-core.version=value"` for Windows, overrides default version of liquibase-core.

To run the test suite itself, you can execute `mvn -Dtest=LiquibaseHarnessSuiteTest test`

## Cleanup

When you are done with test execution, run `docker compose down --volumes` to stop the docker containers 
gracefully and to allow the tests to start from a clean slate on the next run.

## Local testing

The Liquibase Test Harness repository uses [localstack](https://www.localstack.cloud/) and the [awslocal CLI](https://github.com/localstack/awscli-local) to run tests against different `AWS RDS` (`MySQL`, `PostgreSQL`, `MariaDB`, and `Microsoft SQL Server`) databases locally. Read more about it and how to execute tests against local `AWS` database instances [here](README.localstack.md).
