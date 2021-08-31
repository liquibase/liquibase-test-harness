#!/usr/bin/env bash

set -ex

db=$1

if [ $db = "mysql-5.6" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=mysql-DdbVersion=5.6 test
  exit 0
fi

if [ $db = "mysql-8" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=mysql -DdbVersion=8 test
  exit 0
fi

if [ $db = "postgres-12" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=postgresql -DdbVersion=12 -Dmaven.test.failure.ignore=true test
  exit 0
fi

if [ $db = "edb-13" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=edb -DdbVersion=13 test
  exit 0
fi
