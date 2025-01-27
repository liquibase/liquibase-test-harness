#!/usr/bin/env bash

set -ex

db=$1

case $db in

  # percona xtradb cluster needs a bit more time to start
  "percona-xtradb-cluster-5.7"|"percona-xtradb-cluster-8.0"|"percona-xtradb-cluster-8.4" )
    docker compose up -d $db
    sleep 180
    ;;

  # edb setup requires login to private registry
  "edb-postgres-12"|"edb-postgres-13"|"edb-postgres-14"|"edb-postgres-15"|"edb-postgres-16"|"edb-edb-12"|"edb-edb-13"|"edb-edb-14"|"edb-edb-15"|"edb-edb-16")
    docker login $REPO_URL -u $REPO_USER -p $REPO_PASSWORD
    docker compose -f docker-compose.edb.yml up -d $db
    exit 0
    ;;

  "diff")
    docker compose up -d postgres-17 postgres-16 postgres-14 postgres-13 mysql-5.7 mysql-5.6 mysql-8 mysql-8.4 mariadb-10.4 mariadb-10.5 mariadb-10.6 mariadb-10.7 mssql-2017 mssql-2019 mssql-2022
    sleep 40
    docker ps -a
    ;;

  "diffChangelog")
    docker compose up -d postgres-17 postgres-16 postgres-14 postgres-13 mysql-5.7 mysql-5.6 mysql-8 mysql-8.4 mariadb-10.4 mariadb-10.5 mariadb-10.6 mariadb-10.7 mssql-2017 mssql-2019 mssql-2022
    sleep 40
    docker ps -a
    ;;

  # crdb also has an init container
  "crdb-23.1")
    docker compose up -d $db
    sleep 20
    docker compose up -d ${db}-init
    docker compose logs $db
    docker compose logs ${db}-init
    exit 0
    ;;

  "crdb-23.2")
    docker compose up -d $db
    sleep 20
    docker compose up -d ${db}-init
    docker compose logs $db
    docker compose logs ${db}-init
    exit 0
    ;;

  "crdb-24.1")
    docker compose up -d $db
    sleep 20
    docker compose up -d ${db}-init
    docker compose logs $db
    docker compose logs ${db}-init
    exit 0
    ;;

  # in memory and cloud databases
  "derby"|"sqlite"|"H2Database-2.2"|"snowflake")
    exit 0
    ;;

  # titan run databases
  "hsqldb-2.4"|"hsqldb-2.5"|"firebird-3"|"firebird-4")
    ../titan-installer.sh 0.6.0
    docker ps
    titan clone s3web://test-harness-titan-configs.s3-website.us-east-2.amazonaws.com/$db
    exit 0
    ;;

  # informix needs a bit more time to start
  "informix-12.10"|"informix-14.10")
    docker compose up -d $db
    sleep 60
    ;;

  # standard startup
  *)
    docker compose up -d $db
    ;;
esac
