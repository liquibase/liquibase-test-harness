#!/usr/bin/env bash

set -ex

db=$1
tc=$2

test_command () {
  $@
  exit 0
}

case $db in
  "mysql-5.6" )
    test_command mvn -ntp -Dtest=$tc -DdbName=mysql -DdbVersion=5.6 test
    ;;
  "mysql-5.7" )
    test_command mvn -ntp -Dtest=$tc -DdbName=mysql -DdbVersion=5.7 test
    ;;
  "mysql-8" )
    test_command mvn -ntp -Dtest=$tc -DdbName=mysql -DdbVersion=8 test
    ;;
  "percona-xtradb-cluster-5.7" )
    test_command mvn -ntp -Dtest=$tc -DdbName=percona-xtradb-cluster -DdbVersion=5.7 test
    ;;
  "percona-xtradb-cluster-8.0" )
    test_command mvn -ntp -Dtest=$tc -DdbName=percona-xtradb-cluster -DdbVersion=8.0 test
    ;;
  "postgres-12" )
    test_command mvn -ntp -Dtest=$tc -DdbName=postgresql -DdbVersion=12 test
    ;;
  "postgres-13" )
    test_command mvn -ntp -Dtest=$tc -DdbName=postgresql -DdbVersion=13 test
    ;;
  "postgres-14" )
    test_command mvn -ntp -Dtest=$tc -DdbName=postgresql -DdbVersion=14 test
    ;;
  "postgres-15" )
    test_command mvn -ntp -Dtest=$tc -DdbName=postgresql -DdbVersion=15 test
    ;;
  "postgres-16" )
    test_command mvn -ntp -Dtest=$tc -DdbName=postgresql -DdbVersion=16 test
    ;;
  "mariadb-10.2" )
    test_command mvn -ntp -Dtest=$tc -DdbName=mariadb -DdbVersion=10.2 test
    ;;
  "mariadb-10.3" )
    test_command mvn -ntp -Dtest=$tc -DdbName=mariadb -DdbVersion=10.3 test
    ;;
  "mariadb-10.4" )
    test_command mvn -ntp -Dtest=$tc -DdbName=mariadb -DdbVersion=10.4 test
    ;;
  "mariadb-10.5" )
    test_command mvn -ntp -Dtest=$tc -DdbName=mariadb -DdbVersion=10.5 test
    ;;
  "mariadb-10.6" )
    test_command mvn -ntp -Dtest=$tc -DdbName=mariadb -DdbVersion=10.6 test
    ;;
  "mariadb-10.7" )
    test_command mvn -ntp -Dtest=$tc -DdbName=mariadb -DdbVersion=10.7 test
    ;;
  "mssql-2017" )
    test_command mvn -ntp -Dtest=$tc -DdbName=mssql -DdbVersion=2017 test
    ;;
  "mssql-2019" )
    test_command mvn -ntp -Dtest=$tc -DdbName=mssql -DdbVersion=2019 test
    ;;
  "mssql-2022" )
    test_command mvn -ntp -Dtest=$tc -DdbName=mssql -DdbVersion=2022 test
    ;;
  "H2Database-2.2" )
    test_command mvn -ntp -Dtest=$tc -DdbName=h2 test
    ;;
  "crdb-23.1" )
    test_command mvn -ntp -Dtest=$tc -DdbName=cockroachdb -DdbVersion=23.1 test
    ;;
  "crdb-23.2" )
    test_command mvn -ntp -Dtest=$tc -DdbName=cockroachdb -DdbVersion=23.2 test
    ;;
  "crdb-24.1" )
    test_command mvn -ntp -Dtest=$tc -DdbName=cockroachdb -DdbVersion=24.1 test
    ;;
  "edb-postgres-12" )
    test_command mvn -ntp -Dtest=$tc -DdbName=edb-postgres -DdbVersion=12 test
    ;;
  "edb-postgres-13" )
    test_command mvn -ntp -Dtest=$tc -DdbName=edb-postgres -DdbVersion=13 test
    ;;
  "edb-postgres-14" )
    test_command mvn -ntp -Dtest=$tc -DdbName=edb-postgres -DdbVersion=14 test
    ;;
  "edb-postgres-15" )
    test_command mvn -ntp -Dtest=$tc -DdbName=edb-postgres -DdbVersion=15 test
    ;;
  "edb-postgres-16" )
    test_command mvn -ntp -Dtest=$tc -DdbName=edb-postgres -DdbVersion=16 test
    ;;
  "edb-edb-12" )
    test_command mvn -ntp -Dtest=$tc -DdbName=edb-edb -DdbVersion=12 test
    ;;
  "edb-edb-13" )
    test_command mvn -ntp -Dtest=$tc -DdbName=edb-edb -DdbVersion=13 test
    ;;
  "edb-edb-14" )
    test_command mvn -ntp -Dtest=$tc -DdbName=edb-edb -DdbVersion=14 test
    ;;
  "edb-edb-15" )
    test_command mvn -ntp -Dtest=$tc -DdbName=edb-edb -DdbVersion=15 test
    ;;
  "edb-edb-16" )
    test_command mvn -ntp -Dtest=$tc -DdbName=edb-edb -DdbVersion=16 test
    ;;
  "derby" )
    test_command mvn -ntp -Dtest=$tc -DdbName=derby test
    ;;
  "sqlite" )
    test_command mvn -ntp -Dtest=$tc -DdbName=sqlite test
    ;;
  "diff")
    test_command mvn -ntp -Dtest=DiffTest test
    ;;
  "hsqldb-2.4")
    test_command mvn -ntp -Dtest=$tc -DdbName=hsqldb -DdbVersion=2.4 test
    ;;
  "hsqldb-2.5")
    test_command mvn -ntp -Dtest=$tc -DdbName=hsqldb -DdbVersion=2.5 test
    ;;
  "firebird-3")
    test_command mvn -ntp -Dtest=$tc -DdbName=firebird -DdbVersion=3 test
    ;;
  "firebird-4")
    test_command mvn -ntp -Dtest=$tc -DdbName=firebird -DdbVersion=4 test
    ;;
  "db2-luw")
    test_command mvn -ntp -Dtest=$tc -DdbName=db2-luw test
    ;;
  "snowflake")
    test_command mvn -ntp -Dtest=$tc -DdbName=snowflake -DrollbackStrategy=rollbackByTag test
    ;;
  "informix-12.10")
    test_command mvn -ntp -Dtest=$tc -DdbName=informix -DdbVersion=12 test
    ;;
  "informix-14.10")
    test_command mvn -ntp -Dtest=$tc -DdbName=informix -DdbVersion=14 test
    ;;
esac

