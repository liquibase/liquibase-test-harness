#!/usr/bin/env bash

set -ex

db=$1

case $db in
  # edb specific teardown
  "edb-postgres-9.5"|"edb-postgres-9.6"|"edb-postgres-10"|"edb-postgres-11"|"edb-postgres-12"|"edb-postgres-13"|"edb-postgres-14"|"edb-edb-9.5"|"edb-edb-9.6"|"edb-edb-10"|"edb-edb-11"|"edb-edb-12"|"edb-edb-13"|"edb-edb-14")
    docker-compose -f docker-compose.edb.yml down --volumes
    exit 0
    ;;

  # in memory databases
  "derby"|"sqlite")
    exit 0
    ;;

  # titan run databases
  "hsqldb"|"firebird")
    titan uninstall -f
    exit 0
    ;;

  # standard teardown
  *)
    docker-compose down --volumes
    ;;
esac