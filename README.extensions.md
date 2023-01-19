# Using Test Harness in Liquibase Extensions

This test harness is *also* designed to make it easy for you to test your extensions.

## Configuring your project
 
#### Adding test-harness as a dependency

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

#### Configuring your connections

- Add a harness-config.yml file to your `src/test/resources` file. 
  - This file should contain the connection information for all the databases you want your extension to be tested against.
  - *See https://github.com/liquibase/liquibase-test-harness/blob/main/src/test/resources/harness-config.yml as an example.*

#### Setting up your databases

- Your database under test will need to be created prior to the Harness tests running. The test harness also requires certain objects to be pre-created in your database.
- If possible, provide a docker-compose.yml file that starts and configures your test databases. 
- Add a `harness.initScript.sql` file located in the same `src/test/resources` directory to do this preparatory step.
 - *You can refer to an example script file here : https://github.com/liquibase/liquibase-test-harness/blob/main/src/test/resources/sqlite/sqlite-init.sql*

#### Adding a LiquibaseHarnessSuite file

In your `src/test/groovy` directory, create a file with this inclusion:      

```
class ExtensionHarnessTest extends BaseHarnessSuite {

}
```

This suite will run the Base Harness Test Suite

## Adding Additional Tests and Configurations

#### Foundational Test

- If you are creating a new extension to work with Liquibase, it is advisable to start with the Foundational Level Test. 
  - See https://github.com/liquibase/liquibase-test-harness#foundationaltest for a description of what this test validates.
- In your `src/test/groovy/ext` directory, create a file with this inclusion:   

```
class LiquibaseHarnessFoundationalSuiteTest extends FoundationalHarnessSuite {
    }
```
- From your IDE, right click on this `LiquibaseHarnessFoundationalSuiteTest` suite to execute the test.

Alternatively you can:
- Add new run configuration in extension project and choose class `liquibase.harness.compatibility.foundational.FoundationalTest`
from external libraries. Run the test from this configuration.

#### Advanced Test

- This is the test suite that will help validate that your database extension meets Advanced Level Test Criteria. 
  - See https://github.com/liquibase/liquibase-test-harness#advanced-test-suite for a description of what this suite of tests validate.
- In your `src/test/groovy/ext` directory, create a file with this inclusion:   

```
class LiquibaseHarnessAdvancedSuiteTest extends AdvancedHarnessSuite {
    }
```
- From your IDE, right click on this `LiquibaseHarnessAdvancedSuiteTest` suite to execute the test.

#### New Database Extensions

- If your extension is adding support for a new database type, you will be mainly focused on running and capturing the standard test permutations.
- For each of the tests in the framework, we include base permutations that should run successfully. 
  - If there are "validation" files that the test uses, it will write an initial version of the file if it does not exit.
  - If a particular permutation is failing because it does not apply to your database, there is a way to override that functionality.
- Once the standard permutations are passing, you can add additional database-specific permutations as you need. 
- For details on how to configure verifications and add permutations, see read the main readme. 
      
#### New Functionality in existing Extensions

- If your extension is adding support for new functionality into Liquibase, you will mainly be focused on adding new permutations to the existing tests.
  - For example, if you are adding a new `cleanTable` tag, you'd add a new ChangeObjectsTest configuration for that new tag.  
- For details on how to configure verifications and add permutations, please read the main readme. 
   
