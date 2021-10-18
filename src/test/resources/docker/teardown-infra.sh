#!/usr/bin/env bash

set -ex

db=$1

case $db in
  # edb sspecific teardown
  "edb-9.5"|"edb-9.6"|"edb-10"|"edb-11"|"edb-12"|"edb-13")
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