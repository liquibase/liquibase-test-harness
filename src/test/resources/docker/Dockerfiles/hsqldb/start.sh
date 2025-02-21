#!/bin/bash

# Create data directory if it doesn't exist
mkdir -p /opt/hsqldb/data

# Start HSQLDB server in background
java -cp /opt/hsqldb/hsqldb.jar org.hsqldb.server.Server \
    --database.0 file:/opt/hsqldb/data/HSQLDB_DATABASE_ALIAS \
    --dbname.0 HSQLDB_DATABASE_ALIAS &

# Wait for the server to start (simple sleep)
echo "Waiting for HSQLDB to start..."
sleep 3

# Run initialization script
java -cp "/opt/hsqldb/hsqldb.jar:/opt/hsqldb/sqltool.jar" \
    org.hsqldb.cmdline.SqlTool \
    --inlineRc=url=jdbc:hsqldb:hsql://127.0.0.1:9001/HSQLDB_DATABASE_ALIAS,user=SA,password= \
    /opt/hsqldb/init.sql

# Keep container running
wait
