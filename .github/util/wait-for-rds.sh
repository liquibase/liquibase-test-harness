#!/usr/bin/env bash
# wait-for-rds.sh - wait for a LocalStack RDS resource to be ready.
#
# Replaces `sleep 30` in aws.yml init steps. Polls the RDS API for resource
# status (fails fast on error/failed: surfaces unsupported engine versions
# in ~10s instead of as a cryptic JDBC EOFException 35s later), then probes
# the protocol port until the backend actually accepts a handshake.
#
# Usage:
#   source .github/util/wait-for-rds.sh
#   wait_for_rds <cluster|instance> <identifier> <postgres|mysql|mariadb|mssql> <port> [proto_timeout]
#
# The optional 5th arg (or RDS_PROTO_TIMEOUT env var) overrides how long we wait
# for the protocol port to accept a handshake. Docker-backed engines (mysql,
# mssql) cold-start a fresh container: image pull + init can exceed the
# default, so callers can raise it per engine.
set -euo pipefail

wait_for_rds() {
  local resource_type="$1"
  local identifier="$2"
  local protocol="$3"
  local port="$4"

  local api_timeout=300
  local proto_timeout="${5:-${RDS_PROTO_TIMEOUT:-300}}"
  local elapsed=0
  local status

  echo "Waiting for $resource_type '$identifier' (protocol=$protocol, port=$port)..."

  while (( elapsed < api_timeout )); do
    if [[ "$resource_type" == "cluster" ]]; then
      status=$(awslocal rds describe-db-clusters --db-cluster-identifier "$identifier" \
        --query 'DBClusters[0].Status' --output text 2>/dev/null || echo "unknown")
    else
      status=$(awslocal rds describe-db-instances --db-instance-identifier "$identifier" \
        --query 'DBInstances[0].DBInstanceStatus' --output text 2>/dev/null || echo "unknown")
    fi
    case "$status" in
      available)
        echo "  RDS status=available after ${elapsed}s"
        break
        ;;
      error|failed|incompatible-parameters|stopped|deleting)
        echo "  FATAL: $resource_type '$identifier' RDS status=$status : LocalStack rejected the resource (e.g. unsupported engine version or apt-install failure)."
        return 1
        ;;
      *)
        printf '  RDS status=%s, waiting...\n' "$status"
        sleep 5
        elapsed=$((elapsed + 5))
        ;;
    esac
  done
  if (( elapsed >= api_timeout )); then
    echo "  FATAL: $resource_type '$identifier' did not reach status=available within ${api_timeout}s"
    return 1
  fi

  elapsed=0
  while (( elapsed < proto_timeout )); do
    case "$protocol" in
      postgres)
        if pg_isready -h localhost -p "$port" -q 2>/dev/null; then
          echo "  $protocol accepted handshake on port $port after ${elapsed}s (post-API)"
          return 0
        fi
        ;;
      mysql|mariadb)
        # Must force TCP: the mysql client treats "-h localhost" as "use the
        # local UNIX socket" and silently ignores -P, so it never reaches the
        # LocalStack-proxied port. Use 127.0.0.1 + --protocol=TCP. ping returns
        # success even on access-denied: the server responding is all we need.
        if mysqladmin ping -h 127.0.0.1 -P "$port" --protocol=TCP --connect-timeout=3 --silent 2>/dev/null; then
          echo "  $protocol responded to mysqladmin ping on port $port after ${elapsed}s (post-API)"
          return 0
        fi
        ;;
      mssql)
        if timeout 3 bash -c "</dev/tcp/localhost/$port" 2>/dev/null; then
          echo "  $protocol TCP listening on port $port after ${elapsed}s (post-API)"
          return 0
        fi
        ;;
      *)
        echo "  FATAL: unknown protocol '$protocol'"
        return 1
        ;;
    esac
    sleep 3
    elapsed=$((elapsed + 3))
  done

  echo "  FATAL: $protocol on port $port not ready within ${proto_timeout}s after API status=available"
  return 1
}
