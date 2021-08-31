#!/usr/bin/env bash

set -ex

db=$1

# edb setup requires login to private registry
if [ $db = "edb-13" ]; then
  docker login $RT_URL -u $RT_USER -p $RT_PWD
  docker-compose -f docker-compose.edb.yml up -d edb-13
  exit 0
fi

# standard startup
docker-compose up -d $db