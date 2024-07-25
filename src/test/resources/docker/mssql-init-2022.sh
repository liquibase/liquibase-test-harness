#!/usr/bin/env bash

/opt/mssql/bin/sqlservr &

echo "Waiting for server to start...."
#do this in a loop because the timing for when the SQL instance is ready is indeterminate
for i in {1..50};
do
    /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P ${SA_PASSWORD} -C -d master -i /docker-entrypoint-initdb.d/mssql-init.sql
    if [ $? -eq 0 ]
    then
        echo "mssql-init.sh completed"
        break
    else
        echo "not ready yet..."
        sleep 5
    fi
done

sleep infinity
