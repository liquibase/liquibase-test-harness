#!/usr/bin/env bash

set -ex

db=$1
tc=$2
maven_opts="${3:-}"

test_command () {
  $@
  exit 0
}

# Build the Maven command with optional Maven options
build_mvn_command() {
  echo "mvn -ntp $maven_opts"
}

case $db in
  "mysql-8.4" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=mysql -DdbVersion=8.4 test
    ;;
  "mysql-9.7" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=mysql -DdbVersion=9.7 test
    ;;
  "mysql-26.10" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=mysql -DdbVersion=26.10 test
    ;;
  "percona-xtradb-cluster-8.4" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=percona-xtradb-cluster -DdbVersion=8.4 test
    ;;
  "percona-xtradb-cluster-9.7" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=percona-xtradb-cluster -DdbVersion=9.7 test
    ;;
  "postgres-15" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=postgresql -DdbVersion=15 test
    ;;
  "postgres-16" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=postgresql -DdbVersion=16 test
    ;;
  "postgres-17" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=postgresql -DdbVersion=17 test
    ;;
  "postgres-18" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=postgresql -DdbVersion=18 test
    ;;
  "mariadb-10.11" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=mariadb -DdbVersion=10.11 test
    ;;
  "mariadb-11.4" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=mariadb -DdbVersion=11.4 test
    ;;
  "mariadb-11.8" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=mariadb -DdbVersion=11.8 test
    ;;
  "mariadb-12.3" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=mariadb -DdbVersion=12.3 test
    ;;
  "mariadb-13.0" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=mariadb -DdbVersion=13.0 test
    ;;
  "mssql-2017" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=mssql -DdbVersion=2017 test
    ;;
  "mssql-2019" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=mssql -DdbVersion=2019 test
    ;;
  "mssql-2022" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=mssql -DdbVersion=2022 test
    ;;
  "mssql-2025" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=mssql -DdbVersion=2025 test
    ;;
  "H2Database-2.2" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=h2 test
    ;;
  "crdb-25.2" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=cockroachdb -DdbVersion=25.2 test
    ;;
  "crdb-25.4" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=cockroachdb -DdbVersion=25.4 test
    ;;
  "crdb-26.2" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=cockroachdb -DdbVersion=26.2 test
    ;;
  "edb-postgres-15" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=edb-postgres -DdbVersion=15 test
    ;;
  "edb-postgres-16" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=edb-postgres -DdbVersion=16 test
    ;;
  "edb-postgres-18" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=edb-postgres -DdbVersion=18 test
    ;;
  "edb-edb-15" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=edb-edb -DdbVersion=15 test
    ;;
  "edb-edb-16" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=edb-edb -DdbVersion=16 test
    ;;
  "edb-edb-18" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=edb-edb -DdbVersion=18 test
    ;;
  "sqlite" )
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=sqlite test
    ;;
  "diff")
    test_command $(build_mvn_command) -Dtest=DiffTest test
    ;;
  "diffChangelog")
    test_command $(build_mvn_command) -Dtest=DiffChangelogTest test
    ;;
  "hsqldb-2.5")
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=hsqldb -DdbVersion=2.5 test
    ;;
  "hsqldb-2.6")
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=hsqldb -DdbVersion=2.6 test
    ;;
  "hsqldb-2.7")
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=hsqldb -DdbVersion=2.7 test
    ;;
  "firebird-3")
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=firebird -DdbVersion=3 test
    ;;
  "firebird-4")
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=firebird -DdbVersion=4 test
    ;;
  "firebird-5")
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=firebird -DdbVersion=5 test
    ;;
  "db2-luw")
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=db2-luw test
    ;;
  "snowflake")
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=snowflake -DrollbackStrategy=rollbackByTag test
    ;;
  "informix-14.10")
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=informix -DdbVersion=14 test
    ;;
  "informix-15")
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=informix -DdbVersion=15 test
    ;;
  "tidb")
    test_command $(build_mvn_command) -Dtest=$tc -DdbName=mysql -DdbVersion=tidb test
    ;;
esac

