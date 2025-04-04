#!/bin/bash
set -e

# Wait for SQL Server to be ready
echo "Waiting for SQL Server to start..."
for i in {1..60}; do
    if /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -Q "SELECT 1" &> /dev/null; then
        echo "SQL Server started"
        break
    fi
    echo "SQL Server startup in progress (${i}/60)..."
    sleep 1
done

# If SQL Server isn't running, show error and exit
if ! /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -Q "SELECT 1" &> /dev/null; then
    echo "SQL Server failed to start within 60 seconds"
    exit 1
fi

echo "Running initialization script..."
# Run the SQL script for initialization
/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -i /docker-entrypoint-initdb.d/mssql-init.sql

# Create a user for tests if needed
/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -Q "IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'lbuser') BEGIN CREATE LOGIN lbuser WITH PASSWORD = 'LiquibasePass1'; CREATE USER lbuser FOR LOGIN lbuser; END"

# Grant permissions to the user
/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -Q "EXEC sp_addrolemember 'db_owner', 'lbuser'"

echo "SQL Server initialization completed successfully"
