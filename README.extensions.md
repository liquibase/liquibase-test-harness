# Using Test Harness in Liquibase Extensions

Liquibase uses [three verification levels for databases](https://www.liquibase.com/supported-databases/verification-levels): Contributed, Foundational, and Advanced. We created the Liquibase Test Harness to make it easy for you to test your extensions.

This document describes how to configure your database extension to use Liquibase Test Harness. Furthermore, you will learn how to automate, via GitHub Actions, testing the database extension when creating pull requests.

## Configuring your project
 
### Adding test-harness as a dependency

If you are using maven, you can add the dependency as follows:   

```
    <dependencies>
        <dependency>
            <groupId>org.liquibase</groupId>
            <artifactId>liquibase-test-harness</artifactId>
            <version>1.0.9</version> // select the latest version available on maven-central
            <scope>test</scope>
        </dependency>
    </dependencies>
```

### Configuring your connections

- Add a harness-config.yml file to your `src/test/resources` file. 
  - This file should contain the connection information for all the databases you want your extension to be tested against.
  - *See https://github.com/liquibase/liquibase-test-harness/blob/main/src/test/resources/harness-config.yml as an example.*

### Setting up your databases

- Your database under test will need to be created prior to the Harness tests running. The test harness also requires certain objects to be pre-created in your database.
- If possible, provide a docker-compose.yml file that starts and configures your test databases. 
- Add a `harness.initScript.sql` file located in the same `src/test/resources` directory to do this preparatory step.
 - *You can refer to an example script file here : https://github.com/liquibase/liquibase-test-harness/blob/main/src/test/resources/docker/mysql-init.sql*

### Adding a LiquibaseHarnessSuite file

In your `src/test/groovy` directory, create a file with this inclusion:      

```
class ContributedExtensionHarnessSuite extends BaseHarnessSuite {

}
```
- From your IDE, right click on this `ContributedExtensionHarnessSuite` suite to execute the test.

This suite will run the Base Harness Test Suite. Please note the name of the test as it is used by the GitHub Action described below.

## Adding Additional Tests and Configurations

### Foundational Test

- If you are creating a new extension to work with Liquibase, it is advisable to start with the Foundational Level Test. 
  - See https://github.com/liquibase/liquibase-test-harness#foundationaltest for a description of what this test validates.
- Override input files if needed.
  - to override input changelog put it into the path like src/test/resources/liquibase/harness/compatibility/foundational/changelogs/{database_name}/createTable.xml
  - to override checkingSql for 'createTable' changelog, put it into the path like src/test/resources/liquibase/harness/compatibility/foundational/checkingSql/createTable/{database_name}/createTableXml.sql
  - to override expected snapshot of DBCL table data for 'createTable' put it into the path like src/test/resources/liquibase/harness/compatibility/foundational/expectedResultSet/{database_name}/createTable.json
  - *{database_name}* represent value for database_name from harness-config.yml
- In your `src/test/groovy/ext` directory, create a file with this inclusion:   

```
class FoundationalExtensionHarnessSuite extends FoundationalHarnessSuite {

}
```
- From your IDE, right click on this `FoundationalExtensionHarnessSuite` suite to execute the test.

Alternatively you can:
- Add new run configuration in extension project and choose class `liquibase.harness.compatibility.foundational.FoundationalTest`
from external libraries. Run the test from this configuration.

### Advanced Test

- This is the test suite that will help validate that your database extension meets Advanced Level Test Criteria. 
  - See https://github.com/liquibase/liquibase-test-harness#advanced-test-suite for a description of what this suite of tests validate.
- In your `src/test/groovy/ext` directory, create a file with this inclusion:   

```
class AdvancedExtensionHarnessSuite extends AdvancedHarnessSuite {
}
```
- From your IDE, right click on this `AdvancedExtensionHarnessSuite` suite to execute the test.

### New Database Extensions

- If your extension is adding support for a new database type, you will be mainly focused on running and capturing the standard test permutations.
- For each of the tests in the framework, we include base permutations that should run successfully. 
  - If there are "validation" files that the test uses, it will write an initial version of the file if it does not exit.
  - If a particular permutation is failing because it does not apply to your database, there is a way to override that functionality.
- Once the standard permutations are passing, you can add additional database-specific permutations as you need. 
- For details on how to configure verifications and add permutations, see read the main readme. 
      
### New Functionality in existing Extensions

- If your extension is adding support for new functionality into Liquibase, you will mainly be focused on adding new permutations to the existing tests.
  - For example, if you are adding a new `cleanTable` tag, you'd add a new ChangeObjectsTest configuration for that new tag.  
- For details on how to configure verifications and add permutations, please read the main readme. 
   
## Automating Testing using GitHub Actions

Liquibase Test Harness should be run on every pull request for your database extension.

You will need a Docker Compose file for your database, a GitHub Action in `.github/workflows`, and the tests described above.

Not that you will need one Docker Compose file for each database version under test. The naming convention is `docker-compose-[VERSION].yml`.

To take advantage of Liquibase Organization GitHub secrets, your database extension will need to be part of the Liquibase Organization.

Example GitHub Action:

```
name: Liquibase Test Harness

on:
  pull_request:

jobs:
  liquibase-test-harness:
    name: Liquibase Test Harness
    runs-on: ubuntu-latest

    strategy:
      matrix:
        liquibase-support-level: [Contributed, Foundational, Advanced] # Define the different test levels to run
        database-version: [13, 14, 15]
      fail-fast: false # Set fail-fast to false to run all test levels even if some of them fail

    steps:
      - name: Checkout code # Checkout the code from the repository
        uses: actions/checkout@v3

      - name: Start database container # Start the database container using Docker Compose
        run: docker compose -f src/test/resources/docker/docker-compose-${{ matrix.database-version }}.yml up -d

      - name: Setup Temurin Java 17 # Set up Java 17 with Temurin distribution and cache the Maven packages
        uses: actions/setup-java@v3
        with:
          java-version: 17
          distribution: temurin
          cache: 'maven'

      - name: Build with Maven # Build the code with Maven (skip tests)
        run: mvn -ntp -Dmaven.test.skip package

      - name: Run ${{ matrix.liquibase-support-level }} Liquibase Test Harness # Run the Liquibase test harness at each test level
        continue-on-error: true # Continue to run the action even if the previous steps fail
        env:
          LIQUIBASE_PRO_LICENSE_KEY: ${{ secrets.PRO_LICENSE_KEY }} # Set the environment variable for the Liquibase Pro license key
        run: mvn -ntp -DdbVersion=${{ matrix.database-version }} -Dtest=${{ matrix.liquibase-support-level }}ExtensionHarnessSuite test # Run the Liquibase test harness at each test level

      - name: Test Reporter # Generate a test report using the Test Reporter action
        uses: dorny/test-reporter@v1.6.0
        if: always() # Run the action even if the previous steps fail
        with:
          name: Liquibase Test Harness - ${{ matrix.liquibase-support-level }} Reports # Set the name of the test report
          path: target/surefire-reports/TEST-*.xml # Set the path to the test report files
          reporter: java-junit # Set the reporter to use
          fail-on-error: false # Set fail-on-error to false to show report even if it has failed tests

      - name: Stop database container # Stop the database container using Docker Compose
        run: docker compose -f src/test/resources/docker/docker-compose-${{ matrix.database-version }}.yml down
```

On Pull Request creation, each test level will run against each database version. If you would like to test for only a subset of Liquibase Verification Levels, simply remove the levels you do not wish to test against. 