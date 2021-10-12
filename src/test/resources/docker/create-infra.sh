#!/usr/bin/env bash

set -ex

db=$1

case $db in
  # edb setup requires login to private registry
  "edb-9.5"|"edb-9.6"|"edb-10"|"edb-11"|"edb-12"|"edb-13")
    docker login $RT_URL -u $RT_USER -p $RT_PWD
    docker-compose -f docker-compose.edb.yml up -d $db
    exit 0
    ;;

  "diff")
    docker-compose up -d postgres-9 postgres-12 mysql-5.7 mysql-8
    ;;

  # crdb also has an init container
  "crdb-20.2"|"crdb-20.1"|"crdb-21.1")
    docker-compose up -d $db ${db}-init
    exit 0
    ;;

  # in memory databases
  "derby"|"sqlite")
    exit 0
    ;;

  # titan run databases
  "hsqldb-2.4"|"firebird-4")
    ../titan-installer.sh 0.5.3
    docker ps
    titan clone s3web://test-harness-titan-configs.s3-website.us-east-2.amazonaws.com/$db
    exit 0
    ;;

    # titan run databases, for some platforms that have few versions we need to remap ports, as default is occupied by one of them
    "hsqldb-2.5")
      ../titan-installer.sh 0.5.3
      docker ps
      titan clone s3web://test-harness-titan-configs.s3-website.us-east-2.amazonaws.com/$db -P -- -p 9002:9001
      exit 0
      ;;

    "firebird-3")
      ../titan-installer.sh 0.5.3
      docker ps
      titan clone s3web://test-harness-titan-configs.s3-website.us-east-2.amazonaws.com/$db -P -- -p 3051:3050
      exit 0
      ;;

  # standard startup
  *)
    docker-compose up -d $db
    ;;
esac