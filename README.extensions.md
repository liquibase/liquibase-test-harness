# Using Test Harness in Extensions

This test harness is *also* designed to make it easy for you to test your extensions.

## Configuring your project
 
#### Adding as a dependency

If you are using maven, you can add the dependency as follows:   

```
    <dependencies>
        <dependency>
            <groupId>org.liquibase</groupId>
            <artifactId>liquibase-sdk</artifactId>
            <version>4.2.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
```

#### Configuring your connections

Add a liquibase.sdk.test.yml file to your `src/test/resources` file. 
This file should contain the connection information for all the databases you want your extension to be tested against.

See [src/test/resources/liquibase.sdk.test.yml] as an example.

#### Setting up your databases

If possible, provide a docker-compose.yml file that starts and configures your test databases. 

The test harness requires certain objects to be pre-created in your database. See [src/test/resources/docker/postgres-init.sh] as an example of what setup is required.

#### Adding a LiquibaseSdkSuite file

In your `src/test/groovy` directory, create a file like:      

```
class LiquibaseSdkSuite extends liquibase.sdk.test.BaseLiquibaseSdkSuite {

}
```

This suite will run all Test Harness tests

## Adding Additional Tests and Configurations

#### New-Database Extensions

If your extension is adding support for a new database type, you will be mainly focused on running and capturing the standard test permutations.

For each of the tests in the framework, we include base permutations that should run successfully. 
If there are "validation" files that the test uses, it will write an initial version of the file if it does not exit.
If a particular permutation is failing because it does not apply to your database, there is a way to override that functionality.

Once the standard permutations are passing, you can add additional database-specific permutations as you need. 

For details on how to configure verifications and add permutations, see the `Framework Tests` section of [README.md] 
      
#### New Functionality Extensions

If your extension is adding support for new functionality into Liquibase, you will mainly be focused on adding new permutations to the existing tests.
 
For example, if you are adding a new `cleanTable` tag, you'd add a new ChangeObjectsTest configuration for that new tag.  
 
For details on how to configure verifications and add permutations, see the `Framework Tests` section of [README.md] 
   
