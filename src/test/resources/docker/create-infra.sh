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
  "edb-9.5"|"edb-9.6"|"edb-10"|"edb-11"|"edb-12"|"edb-13")
    docker login $ARTIFACTORY_URL -u $ARTIFACTORY_USER -p $ARTIFACTORY_TOKEN
    docker-compose -f docker-compose.edb.yml up -d $db
    exit 0
    ;;

  "diff")
    docker-compose up -d postgres-9 postgres-12 mysql-5.7 mysql-8
    ;;

  # crdb also has an init container
  "crdb-20.2"|"crdb-20.1"|"crdb-21.1"|"crdb-21.2")
    docker-compose up -d $db ${db}-init
    exit 0
    ;;

  # in memory databases
  "derby"|"sqlite")
    exit 0
    ;;

  # titan run databases
  "hsqldb-2.4"|"hsqldb-2.5"|"firebird-3"|"firebird-4")
    ../titan-installer.sh 0.5.3
    docker ps
    titan clone s3web://test-harness-titan-configs.s3-website.us-east-2.amazonaws.com/$db
    exit 0
    ;;

  # standard startup
  *)
    docker-compose up -d $db
    ;;
esac