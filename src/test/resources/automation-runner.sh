#!/usr/bin/env bash

set -ex

db=$1

test_command () {
  $@
  exit 0
}

case $db in
  "mysql-5.6" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=mysql -DdbVersion=5.6 test
    ;;
  "mysql-5.7" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=mysql -DdbVersion=5.7 test
    ;;
  "mysql-8" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=mysql -DdbVersion=8 test
    ;;
  "postgres-9.5" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=postgresql -DdbVersion=9.5 -Dmaven.test.failure.ignore=true test
    ;;
  "postgres-9" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=postgresql -DdbVersion=9 -Dmaven.test.failure.ignore=true test
    ;;
  "postgres-10" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=postgresql -DdbVersion=10 -Dmaven.test.failure.ignore=true test
    ;;
  "postgres-11" )
    test_command  mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=postgresql -DdbVersion=11 -Dmaven.test.failure.ignore=true test
    ;;
  "postgres-12" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=postgresql -DdbVersion=12 -Dmaven.test.failure.ignore=true test
    ;;
  "postgres-13" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=postgresql -DdbVersion=13 -Dmaven.test.failure.ignore=true test
    ;;
  "mariadb-10.3" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=mariadb -DdbVersion=10.3 test
    ;;
  "mariadb-10.5" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=mariadb -DdbVersion=10.5 test
    ;;
  "mssql-2017" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=mssql -DdbVersion=2017 test
    ;;
  "mssql-2019" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=mssql -DdbVersion=2019 test
    ;;
  "H2Database-1.4" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=h2 test
    ;;
  "crdb-20.2" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=cockroachdb -DdbVersion=20.2 test
    ;;
  "crdb-20.1" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=cockroachdb -DdbVersion=20.1 test
    ;;
  "crdb-21.1" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=cockroachdb -DdbVersion=21.1 test
    ;;
  "edb-9.5" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=edb -DdbVersion=9.5 test
    ;;
  "edb-9.6" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=edb -DdbVersion=9.6 test
    ;;
  "edb-10" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=edb -DdbVersion=10 test
    ;;
  "edb-11" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=edb -DdbVersion=11 test
    ;;
  "edb-12" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=edb -DdbVersion=12 test
    ;;
  "edb-13" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=edb -DdbVersion=13 test
    ;;
  "derby" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=derby test
    ;;
  "sqlite" )
    test_command mvn -Dtest=LiquibaseHarnessSuiteTest -DdbName=sqlite -Dmaven.test.failure.ignore=true test
    ;;
esac

