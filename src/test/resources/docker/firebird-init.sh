#!/usr/bin/env bash
set -e
/usr/local/firebird/bin/isql -user lbuser -password LiquibasePass1 -i /docker-entrypoint-initdb.d/firebird-init.sql lbcat