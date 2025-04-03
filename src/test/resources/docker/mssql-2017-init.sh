#!/bin/bash

echo "Waiting for SQL Server to start..."
MAX_RETRIES=30
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    # Try to connect to SQL Server
    if /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "LiquibasePass1" -Q "SELECT 1" &>/dev/null; then
        echo "SQL Server started successfully"
        break
    fi
    
    echo "Waiting for SQL Server to start (Attempt $((RETRY_COUNT+1))/$MAX_RETRIES)..."
    RETRY_COUNT=$((RETRY_COUNT+1))
    sleep 5
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo "Failed to connect to SQL Server after $MAX_RETRIES attempts"
    exit 1
fi

echo "Creating database lbcat..."
/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "LiquibasePass1" -Q "IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'lbcat') CREATE DATABASE lbcat"

echo "Running initialization script..."
/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "LiquibasePass1" -d lbcat -i /mssql-init.sql

echo "SQL Server initialization completed successfully"
