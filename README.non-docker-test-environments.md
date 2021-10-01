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


## Running tests against docker containers managed by Titan
Here in Liquibase we use [Titan](https://titan-data.io/) to manage containers that require additional configurations
in contrast to just running docker container from DockerHub image.
Some images have their own init scripts, so we can't run our script that populate test DB with data during container start.
Fortunately Titan allow us to make container snapshot at any moment, push this snapshot to some common place like AWS S3 bucket
and with a single Titan command pull and start container from that snapshot. 

If you want to run container with DB up and ready just like we do - install titan, add path to it's executive to your environment variables,
run `titan install` and `titan clone s3web://test-harness-titan-configs.s3-website.us-east-2.amazonaws.com/{DbName}` from your Terminal.
It will pull the image and run container from snapshot with already properly initialized DB.
More information about installing Titan you can find on a website.

If that sounds too complicated, or you don't want to install Titan for some reason, you can uncomment section in 
`src/test/resources/docker/docker-compose.yml` that is corresponding to platform you are interested in, connect to DB, copy and paste 
SQL statements from `src/test/resources/docker{DnName}-init.sql`. And here you do - now you can run tests against this platform