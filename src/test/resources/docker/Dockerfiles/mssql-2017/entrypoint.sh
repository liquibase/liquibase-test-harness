#!/bin/bash
set -e

# Print out environment and system info for debugging
echo "===== BEGIN SYSTEM INFO ====="
echo "Date: $(date)"
echo "Hostname: $(hostname)"
echo "Memory:"
free -h || echo "free command not available"
echo "Disk space:"
df -h || echo "df command not available"
echo "===== END SYSTEM INFO ====="

# Create directories with proper permissions
mkdir -p /var/opt/mssql/data
mkdir -p /var/opt/mssql/log
mkdir -p /var/opt/mssql/dump
mkdir -p /docker-entrypoint-initdb.d

# Set up locale
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8

# Ensure proper permissions for SQL Server directories
# Use -f flag to suppress errors if we can't chown in certain environments
chown -Rf mssql:mssql /var/opt/mssql || echo "Warning: Could not chown /var/opt/mssql, continuing anyway"
chmod -Rf 775 /var/opt/mssql || echo "Warning: Could not chmod /var/opt/mssql, continuing anyway"

# Try to set core dump pattern, but continue if it fails (read-only filesystem in GitHub Actions)
echo "Setting core dump pattern..."
if [ -w /proc/sys/kernel/core_pattern ]; then
    echo "/var/opt/mssql/dump/core.%e.%p" > /proc/sys/kernel/core_pattern
else
    echo "Warning: Cannot write to /proc/sys/kernel/core_pattern (read-only filesystem), skipping"
fi

# Try to set ulimit, but continue if it fails
ulimit -c unlimited || echo "Warning: Could not set ulimit for core dumps, continuing anyway"

# Configure SQL Server with minimal memory footprint
# Create the configuration file directory
mkdir -p /var/opt/mssql/mssql.conf.d

# Write config file with memory limits and debug settings
echo "Writing minimal configuration..."
cat > /var/opt/mssql/mssql.conf << EOF
[memory]
memorylimitmb = 1024

[sqlagent]
enabled = false

[filelocation]
defaultdatadir = /var/opt/mssql/data
defaultlogdir = /var/opt/mssql/log
defaultdumpdir = /var/opt/mssql/dump

[telemetry]
customerfeedback = false

[network]
tcpport = 1433
ipaddress = 0.0.0.0
EOF

# Display SQL Server config
echo "Final SQL Server configuration:"
cat /var/opt/mssql/mssql.conf

# Accept EULA and set SA password
if [ -n "$SA_PASSWORD" ]; then
    echo "Configuring SQL Server with provided SA password..."
    
    # Export environment variables
    export MSSQL_SA_PASSWORD=$SA_PASSWORD
    export MSSQL_PID=Developer
    export ACCEPT_EULA=Y
    
    # Run setup with debug output and error handling
    echo "Running mssql-conf setup..."
    /opt/mssql/bin/mssql-conf -n setup accept-eula || {
        echo "mssql-conf setup failed with error code $?, trying alternate approach"
        # Touch files to indicate EULA acceptance
        mkdir -p /var/opt/mssql/mssql.conf.d || true
        touch /var/opt/mssql/mssql.conf.d/eula_accepted || true
    }
    
    # Check if master.mdf exists
    echo "Checking for master.mdf..."
    find /var/opt/mssql -name "master.mdf" || echo "master.mdf not found"
    
    # Remove any pid file that might be left over
    rm -f /var/run/sqlservr.pid

    # Start SQL Server in the foreground - this is critical for container stability
    echo "Starting SQL Server in managed mode..."
    
    # First start in background to initialize, so we can run scripts
    /opt/mssql/bin/sqlservr &
    pid=$!
    echo $pid > /var/run/sqlservr.pid
    
    # Wait for SQL Server to start
    echo "Waiting for SQL Server to start..."
    for i in {1..60}; do
        if /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -Q "SELECT @@VERSION" &> /dev/null; then
            echo "SQL Server started successfully"
            break
        fi
        
        # Check if process is still running
        if ! kill -0 $pid 2>/dev/null; then
            echo "SQL Server process died. Checking logs..."
            tail -n 100 /var/opt/mssql/log/errorlog || echo "Error log not found"
            exit 1
        fi
        
        echo "Waiting for SQL Server to start (attempt $i/60)..."
        sleep 1
    done
    
    # Configure SQL Server network settings to ensure TCP/IP is enabled
    echo "Configuring SQL Server network settings..."
    /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -Q "EXEC sp_configure 'show advanced options', 1; RECONFIGURE; EXEC sp_configure 'remote access', 1; RECONFIGURE;" || echo "Warning: Could not configure remote access"
    
    # Run initialization scripts if they exist
    if [ -d "/docker-entrypoint-initdb.d" ]; then
        echo "Running initialization scripts..."
        
        # Run SQL scripts first
        echo "Looking for SQL scripts..."
        for f in /docker-entrypoint-initdb.d/*.sql; do
            if [ -f "$f" ]; then
                echo "Running SQL script: $f"
                /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P "$SA_PASSWORD" -i "$f" || echo "Warning: Script $f had errors"
            fi
        done
        
        # Run shell scripts - now also run mssql-init.sh if it exists
        echo "Looking for shell scripts..."
        for f in /docker-entrypoint-initdb.d/*.sh; do
            if [ -f "$f" ]; then
                echo "Running shell script: $f"
                chmod +x "$f"
                "$f" || echo "Warning: Script $f had errors"
            fi
        done
        
        echo "Initialization complete"
    fi
    
    # Signal handling for clean shutdown
    cleanup() {
        echo "Received signal. Shutting down SQL Server gracefully..."
        kill -TERM $pid 2>/dev/null
        wait $pid
        exit 0
    }
    
    # Set up signal trapping
    trap cleanup SIGTERM SIGINT
    
    # Keep container running with SQL Server
    echo "SQL Server is ready for connections"
    echo "Container will continue running in the background"
    
    # Wait for SQL Server process - critical for container stability
    wait $pid
    exit_code=$?
    echo "SQL Server process exited with code $exit_code"
    if [ $exit_code -ne 0 ]; then
        echo "Checking SQL Server error log..."
        tail -n 100 /var/opt/mssql/log/errorlog || echo "Error log not found"
    fi
    exit $exit_code
else
    echo "ERROR: SA_PASSWORD environment variable must be set"
    exit 1
fi
