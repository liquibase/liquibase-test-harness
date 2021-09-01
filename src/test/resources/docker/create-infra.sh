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

  # crdb also has an init container
  "crdb-20.2"|"crdb-20.1"|"crdb-21.1")
    docker-compose up -d $db ${db}-init
    exit 0
    ;;

  # in memory databases
  "derby"|"sqlite")
    exit 0
    ;;

  # standard startup
  *)
    docker-compose up -d $db
    ;;
esac