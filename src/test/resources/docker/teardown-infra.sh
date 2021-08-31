#!/usr/bin/env bash

set -ex

db=$1

# edb specific teardown
if [ $db = "edb-13" ]; then
   docker-compose -f docker-compose.edb.yml down --volumes
  exit 0
fi

# standard teardown
docker-compose down --volumes