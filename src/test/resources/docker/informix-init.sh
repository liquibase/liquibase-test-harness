#!/bin/bash
# Wait for Informix to be ready
echo "Waiting for Informix to be ready..."
while ! grep -q "listener thread is ready" /opt/ibm/informix/logs/online.log; do
    sleep 5
done

# Execute the initialization SQL script
echo "Initializing Informix database..."
dbaccess - <<EOF
$(cat /docker-entrypoint-initdb.d/informix-init.sql)
EOF

echo "Database initialization completed."