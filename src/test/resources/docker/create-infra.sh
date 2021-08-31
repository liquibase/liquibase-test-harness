#!/usr/bin/env bash

set -ex

db=$1

# edb setup requires login to private registry
if [ $db = "edb-9.5" || $db = "edb-9.6" || $db = "edb-10" || $db = "edb-11" || $db = "edb-12" || $db = "edb-13" ]; then
  docker login $RT_URL -u $RT_USER -p $RT_PWD
  docker-compose -f docker-compose.edb.yml up -d edb-13
  exit 0
fi

# crdb also has an init container
if [ $db = "crdb-20.2" || $db =  "crdb-20.1" || $db = "crdb-21.1" ]; then
docker-compose up -d $db ${db}-init
  exit 0
fi

# standard startup
docker-compose up -d $db