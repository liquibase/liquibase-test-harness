#!/usr/bin/env bash
set -e
password='password'

PGPASSWORD=$(echo $password) psql -h localhost -U jenkinsci -d jenkinsci -p 8544 -f edb-init.sql