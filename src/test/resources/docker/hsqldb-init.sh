#!/usr/bin/env bash

java -jar opt/hsqldb/sqltool.jar --rcFile /docker-entrypoint-initdb.d/hsqldb.rc lbcat /docker-entrypoint-initdb.d/hsqldb-init.sql