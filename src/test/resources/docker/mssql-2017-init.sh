#!/bin/bash

set -e

# Wait for SQL Server to be available
function wait_for_sql() {
    for i in {1..60}; do
        # Check if SQL Server is up and running
        /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "LiquibasePass1" -Q "SELECT 1" > /dev/null 2>&1
        if [ $? -eq 0 ]; then
            echo "SQL Server is up and running"
            return 0
        fi
        echo "Waiting for SQL Server to start... ($i/60)"
        sleep 1
    done
    echo "Timeout waiting for SQL Server to start"
    return 1
}

# Wait for SQL Server to be ready
echo "Waiting for SQL Server to be ready..."
wait_for_sql

echo "Creating database lbcat..."
/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "LiquibasePass1" -Q "CREATE DATABASE lbcat"

echo "Running initialization script..."
/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "LiquibasePass1" -d lbcat -i /mssql-init.sql

echo "SQL Server initialization completed"
