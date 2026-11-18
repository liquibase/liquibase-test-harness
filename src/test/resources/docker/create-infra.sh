#!/usr/bin/env bash

set -ex

db=$1

case $db in

  # percona xtradb cluster needs a bit more time to start
  "percona-xtradb-cluster-8.4"|"percona-xtradb-cluster-9.7" )
    docker compose up -d $db
    sleep 180
    ;;

  # edb setup requires login to private registry
  "edb-postgres-15"|"edb-postgres-16"|"edb-postgres-18"|"edb-edb-15"|"edb-edb-16"|"edb-edb-18")
    docker login $REPO_URL -u $REPO_USER -p $REPO_PASSWORD
    docker compose -f docker-compose.edb.yml up -d $db
    exit 0
    ;;

  "diff")
    docker compose up -d postgres-18 postgres-17 postgres-16 postgres-15 mysql-8.4 mysql-9.7 mariadb-10.11 mariadb-11.4 mariadb-11.8 mariadb-12.3 mssql-2017 mssql-2019 mssql-2022
    sleep 40
    docker ps -a
    ;;

  "diffChangelog")
    docker compose up -d postgres-18 postgres-17 postgres-16 postgres-15 mysql-8.4 mysql-9.7 mariadb-10.11 mariadb-11.4 mariadb-11.8 mariadb-12.3 mssql-2017 mssql-2019 mssql-2022
    sleep 40
    docker ps -a
    ;;

  # crdb also has an init container
  "crdb-25.2"|"crdb-25.4"|"crdb-26.2")
    docker compose up -d $db
    sleep 20
    docker compose up -d ${db}-init
    docker compose logs $db
    docker compose logs ${db}-init
    exit 0
    ;;

  # in memory and cloud databases
  "sqlite"|"H2Database-2.2"|"snowflake")
    exit 0
    ;;

  "hsqldb-2.5"|"hsqldb-2.6"|"hsqldb-2.7"|"firebird-3"|"firebird-4"|"firebird-5")
    docker compose up -d $db
    sleep 20
    exit 0
    ;;

  # informix needs a bit more time to start
  "informix-14.10")
    docker compose up -d $db
    sleep 60
    ;;

  # standard startup
  *)
    docker compose up -d $db
    ;;
esac
