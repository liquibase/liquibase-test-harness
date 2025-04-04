#!/bin/bash
set -e

# Configure SQL Server with minimal memory footprint
echo "Configuring SQL Server..."
# Create directories with proper permissions
mkdir -p /var/opt/mssql/data
mkdir -p /var/opt/mssql/log
mkdir -p /var/opt/mssql/dump

# Ensure proper permissions for SQL Server directories
chown -R mssql:mssql /var/opt/mssql
chmod -R 775 /var/opt/mssql

# Configure SQL Server with limited memory
# Create the configuration file directory
mkdir -p /var/opt/mssql/mssql.conf.d

# Write config file with memory limits
cat > /var/opt/mssql/mssql.conf << EOF
[memory]
memorylimitmb = 1024

[filelocation]
defaultdatadir = /var/opt/mssql/data
defaultlogdir = /var/opt/mssql/log
defaultdumpdir = /var/opt/mssql/dump

[telemetry]
customerfeedback = false

[sqlagent]
enabled = false
EOF

# Accept EULA and set SA password
if [ -n "$SA_PASSWORD" ]; then
    # Configure SQL Server
    export MSSQL_SA_PASSWORD=$SA_PASSWORD
    
    # Setup SQL Server with configuration
    /opt/mssql/bin/mssql-conf -n setup accept-eula
    
    # Start SQL Server
    echo "Starting SQL Server with limited resources..."
    # Run SQL Server with ulimit constraints to avoid excessive memory usage
    su -c "ulimit -v 2048000 && /opt/mssql/bin/sqlservr" mssql &
    pid=$!
    
    # Wait for SQL Server to start
    echo "Waiting for SQL Server to start..."
    for i in {1..90}; do
        if /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -Q "SELECT 1" &> /dev/null; then
            echo "SQL Server started successfully"
            break
        fi
        
        # Check if process is still running
        if ! kill -0 "$pid" 2>/dev/null; then
            echo "SQL Server process failed. Checking logs:"
            cat /var/opt/mssql/log/errorlog || echo "No error log found"
            exit 1
        fi
        
        echo "SQL Server startup in progress (${i}/90)..."
        sleep 1
    done
    
    # If SQL Server didn't start, exit
    if ! /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -Q "SELECT 1" &> /dev/null; then
        echo "SQL Server failed to start. Checking logs:"
        cat /var/opt/mssql/log/errorlog || echo "No error log found"
        exit 1
    fi
    
    # Run initialization scripts
    if [ -d "/docker-entrypoint-initdb.d" ]; then
        echo "Running initialization scripts..."
        
        # Run SQL scripts
        for f in /docker-entrypoint-initdb.d/*.sql; do
            if [ -f "$f" ]; then
                echo "Running SQL script: $f"
                /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -i "$f" || echo "Warning: Script $f had errors"
            fi
        done
        
        # Run shell scripts
        for f in /docker-entrypoint-initdb.d/*.sh; do
            if [ -f "$f" ] && [ "$(basename $f)" != "mssql-init.sh" ]; then
                echo "Running shell script: $f"
                chmod +x "$f"
                "$f" || echo "Warning: Script $f had errors"
            fi
        done
        
        echo "Initialization complete"
    fi
    
    # Keep container running with SQL Server
    echo "SQL Server is ready for connections"
    wait "$pid"
else
    echo "ERROR: SA_PASSWORD environment variable must be set"
    exit 1
fi
