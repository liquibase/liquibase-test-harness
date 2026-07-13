#!/usr/bin/env bash

/opt/mssql/bin/sqlservr &

# Pick the sqlcmd client available in this image. SQL Server 2017/2019/2022 ship
# /opt/mssql-tools; SQL Server 2025 ships only /opt/mssql-tools18, which defaults to
# encrypted connections and needs -C to trust the self-signed server certificate.
if [ -x /opt/mssql-tools/bin/sqlcmd ]; then
    SQLCMD="/opt/mssql-tools/bin/sqlcmd"
else
    SQLCMD="/opt/mssql-tools18/bin/sqlcmd -C"
fi

echo "Waiting for server to start...."
#do this in a loop because the timing for when the SQL instance is ready is indeterminate
for i in {1..50};
do
    ${SQLCMD} -S localhost -U sa -P ${SA_PASSWORD} -d master -i /docker-entrypoint-initdb.d/mssql-init.sql
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
