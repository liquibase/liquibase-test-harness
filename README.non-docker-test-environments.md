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
    * Be aware, we use same ports for different versions of same platform in our CI/CD. Change host port for the container if you want to run two versions simultaneously. 
    And change port in connection url in `harness-config.yml` accordingly.
  * Connect to the database 
  * Copy and paste the SQL statements from `src/test/resources/docker{DbName}-init.sql`. 
  * And now you can run tests against this platform

### Adding new Titan managed platform by HSQLDB 2.5 example
##### Remark - this is not comprehensive guide that can teach all you need about Titan, just an example how we do it. Read [Titan docs](https://titan-data.io/docs) to get idea how things work.
* Find container in docker hub that suits Titan. Titan required at least one volume declared in docker image; 
* Run this image with Docker to see if it's working properly, figure out correct container configuration, if you can connect to DB, run some sql commands, etc.;
* Make sure your Titan is up and running
* Run controlled container like this `titan run -e HSQLDB_USER=lbuser -e HSQLDB_PASSWORD=LiquibasePass1 -e HSQLDB_DATABASE_ALIAS=lbcat -n hsqldb-2.5  mitchtalmadge/hsqldb:2.5.0`
  * `-e` is env variable for container, `-n` is name. 
  * To remap ports add next flags `-P -- -p 9002:9001`. `-P` disables the default port mapping `--` allows passing context specific arguments (context is docker in this case).
* Connect to DB and run init script to populate it with test data. Init script for HSQLDB is in `src/test/resources/docker/hsqldb-init.sql`.
* Commit changes `titan commit -m "data loaded" hsqldb-2.5`.
* Make sure you have write access to folder in S3 bucket and your AWS cli credentials are configured. For our case it's `test-harness-titan-configs` bucket with `hsqldb-2.5` folder.
* Add remote, for our case it's `titan remote add s3://test-harness-titan-configs/hsqldb-2.5 hsqldb-2.5`.
* Run `titan push hsqldb-2.5`. It will push the latest commit to s3 bucket.
* Verify two files and one folder with archives is present in s3 bucket.
* Objects in the bucket need to be made public so `s3web` will work for public datasets.
* Run `titan clone s3://test-harness-titan-configs/hsqldb-2.5 -n hsqldb-2.5` to pull image snapshot and run controlled container on other machine, use port remapping (`-P -- -p 9002:9001` similar to `titan run`) if needed.