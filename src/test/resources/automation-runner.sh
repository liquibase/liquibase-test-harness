#!/usr/bin/env bash

set -ex

db=$1

if [ $db = "mysql-5.6" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=mysql -DdbVersion=5.6 test
  exit 0
fi

if [ $db = "mysql-5.7" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=mysql -DdbVersion=5.7 test
  exit 0
fi

if [ $db = "mysql-8" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=mysql -DdbVersion=8 test
  exit 0
fi

if [ $db = "postgres-9.5" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=postgresql -DdbVersion=9.5 -Dmaven.test.failure.ignore=true test
  exit 0
fi

if [ $db = "postgres-9" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=postgresql -DdbVersion=9 -Dmaven.test.failure.ignore=true test
  exit 0
fi

if [ $db = "postgres-10" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=postgresql -DdbVersion=10 -Dmaven.test.failure.ignore=true test
  exit 0
fi

if [ $db = "postgres-11" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=postgresql -DdbVersion=11 -Dmaven.test.failure.ignore=true test
  exit 0
fi

if [ $db = "postgres-12" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=postgresql -DdbVersion=12 -Dmaven.test.failure.ignore=true test
  exit 0
fi

if [ $db = "postgres-13" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=postgresql -DdbVersion=13 -Dmaven.test.failure.ignore=true test
  exit 0
fi

if [ $db = "mariadb-10.3" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=mariadb -DdbVersion=10.3 test
  exit 0
fi

if [ $db = "mariadb-10.5" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=mariadb -DdbVersion=10.5 test
  exit 0
fi

if [ $db = "mssql-2017" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=mssql -DdbVersion=2017 test
  exit 0
fi

if [ $db = "mssql-2019" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=mssql -DdbVersion=2019 test
  exit 0
fi

if [ $db = "H2Database-1.4" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=h2 test
  exit 0
fi

if [ $db = "crdb-20.2" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=cockroachdb -DdbVersion=20.2 test
  exit 0
fi

if [ $db = "crdb-20.1" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=cockroachdb -DdbVersion=20.1 test
  exit 0
fi

if [ $db = "crdb-21.1" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=cockroachdb -DdbVersion=21.1 test
  exit 0
fi

if [ $db = "edb-9.5" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=edb -DdbVersion=9.5 test
  exit 0
fi

if [ $db = "edb-9.6" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=edb -DdbVersion=9.6 test
  exit 0
fi

if [ $db = "edb-10" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=edb -DdbVersion=10 test
  exit 0
fi

if [ $db = "edb-11" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=edb -DdbVersion=11 test
  exit 0
fi

if [ $db = "edb-12" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=edb -DdbVersion=12 test
  exit 0
fi

if [ $db = "edb-13" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=edb -DdbVersion=13 test
  exit 0
fi

if [ $db = "derby" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=derby test
  exit 0
fi

if [ $db = "sqlite" ]; then
  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=sqlite -Dmaven.test.failure.ignore=true test
  exit 0
fi
