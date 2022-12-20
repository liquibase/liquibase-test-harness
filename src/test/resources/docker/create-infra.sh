#!/usr/bin/env bash

set -ex

db=$1

case $db in

  # percona xtradb cluster needs a bit more time to start
  "percona-xtradb-cluster-5.7"|"percona-xtradb-cluster-8.0" )
    docker-compose up -d $db
    sleep 180
    ;;

  # edb setup requires login to private registry
  "edb-postgres-9.5"|"edb-postgres-9.6"|"edb-postgres-10"|"edb-postgres-11"|"edb-postgres-12"|"edb-postgres-13"|"edb-postgres-14"|"edb-edb-9.5"|"edb-edb-9.6"|"edb-edb-10"|"edb-edb-11"|"edb-edb-12"|"edb-edb-13"|"edb-edb-14")
    docker login $ARTIFACTORY_URL -u $ARTIFACTORY_USER -p $ARTIFACTORY_TOKEN
    docker-compose -f docker-compose.edb.yml up -d $db
    exit 0
    ;;

  "diff")
    docker-compose up -d postgres-14 postgres-13 postgres-11 postgres-12 postgres-10 mysql-5.7 mysql-8 mariadb-10.2 mariadb-10.3 mariadb-10.4 mariadb-10.5 mariadb-10.6 mariadb-10.7 mssql-2017 mssql-2019 mssql-2022
    sleep 20
    docker ps -a
    ;;

  # crdb also has an init container
  "crdb-20.2"|"crdb-21.1"|"crdb-21.2"|"crdb-22.1")
    docker-compose up -d $db
    sleep 20
    docker-compose up -d ${db}-init
    docker-compose logs $db
    docker-compose logs ${db}-init
    exit 0
    ;;

  # in memory and cloud databases
  "derby"|"sqlite"|"H2Database-2.1"|"snowflake")
    exit 0
    ;;

  # titan run databases
  "hsqldb-2.4"|"hsqldb-2.5"|"firebird-3"|"firebird-4")
    ../titan-installer.sh 0.6.0
    docker ps
    titan clone s3web://test-harness-titan-configs.s3-website.us-east-2.amazonaws.com/$db
    exit 0
    ;;

  # standard startup
  *)
    docker-compose up -d $db
    ;;
esac
