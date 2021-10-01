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
* Here at Liquibase we use [Titan](https://github.com/titan-data/titan) to manage containers that require additional configurations
instead of running docker containers based on images from DockerHub.
  * Some images have their own init scripts that execute on start up, which means we cannot run our own init scripts to populate the test databases with data during container start up.
  * However Titan allows users to make container snapshots at any given moment of time, then push this snapshot to a staging area like an AWS S3 bucket.
Then with a single Titan command, users can pull and start container from that stored snapshot. 

* So if you want to run a container with the database up and ready just like we do here with the test-harness - 
  * Install Titan
  * Add the path to it's executable to your environment variables
  * Run `titan install` and `titan clone s3web://test-harness-titan-configs.s3-website.us-east-2.amazonaws.com/{DbName}` from your Terminal.
    * Titan will pull the image and startup the container from the snapshot with already initialized database.

For more information about installing Titan, please go the website above.

* But if all this sounds too complicated, or if you prefer not to install Titan: 
  * You can uncomment the section in `src/test/resources/docker/docker-compose.yml` which corresponds to the platform you are interested in
  * Connect to the database 
  * Copy and paste the SQL statements from `src/test/resources/docker{DnName}-init.sql`. 
  * And now you can run tests against this platform
