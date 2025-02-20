## Running tests against cloud or on-prem database instances

You can also choose to run tests from this harness against other test database environments (i.e.which are not a dockerized containers). 
For instance, you may wish to run against a database running in the cloud or a native on-prem installation.

## Getting Started Guide
#### The execution & writing of tests will remain the same -- but you will need to recreate the starting test environment yourself. See below for instructions:
1. In the `harness-config.yml` file, edit the jdbc URL to point to the hostname for your database.
    1. If your username and/or password is different, you will need to modify those values as well.
1. Ensure that you set up the test environment before executing the test-harness tests
    1. For example, if you need to run the tests against a postgres instance, then:
        1. You will need to manually create the `lbcat` database on your postgres database. 
        For example, you can run: `CREATE DATABASE "lbcat";`
        1. You will need to manually execute the sql scripts from the `postgres-init.sh` (located at `src/test/resources/docker/postgres-init.sh`)
        script, so that the database is pre-populated with the test database objects.
