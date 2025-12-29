#!/usr/bin/env bash

set -ex

db=$1

case $db in
  # edb specific teardown
  "edb-postgres-12"|"edb-postgres-13"|"edb-postgres-14"|"edb-postgres-15"|"edb-postgres-16"|"edb-edb-12"|"edb-edb-13"|"edb-edb-14"|"edb-edb-15"|"edb-edb-16")
    docker compose -f docker-compose.edb.yml down --volumes
    exit 0
    ;;

  # in memory databases
  "sqlite")
    exit 0
    ;;

  # standard teardown
  *)
    docker compose down --volumes
    ;;
esac