# Base OS layer: Ubuntu 16.04 LTS
FROM --platform=linux/amd64 ubuntu:16.04

# Install prerequistes including debugging tools
RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install -yq curl apt-transport-https gnupg locales procps iputils-ping net-tools \
                        gdb strace ltrace vim less sudo && \
    # Configure locale to ensure UTF-8 support
    locale-gen en_US.UTF-8 && \
    # Get official Microsoft repository configuration
    curl https://packages.microsoft.com/keys/microsoft.asc | apt-key add - && \
    curl https://packages.microsoft.com/config/ubuntu/16.04/mssql-server-2017.list | tee /etc/apt/sources.list.d/mssql-server.list && \
    curl https://packages.microsoft.com/config/ubuntu/16.04/prod.list | tee /etc/apt/sources.list.d/msprod.list && \
    apt-get update && \
    # Install SQL Server with minimal packages - specify a slightly older, known-working version
    ACCEPT_EULA=Y apt-get install -y mssql-server && \
    # Install SQL Server command-line tools 
    ACCEPT_EULA=Y apt-get install -y mssql-tools unixodbc-dev && \
    # Add SQL Server tools to PATH
    echo 'export PATH="$PATH:/opt/mssql-tools/bin"' >> ~/.bashrc && \
    # Cleanup
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Create directories with proper permissions
RUN mkdir -p /var/opt/mssql && \
    mkdir -p /docker-entrypoint-initdb.d && \
    chown -R mssql:mssql /var/opt/mssql && \
    chmod -R 775 /var/opt/mssql

# Configure environment variables
ENV ACCEPT_EULA=Y
ENV MSSQL_PID=Developer 
ENV LANG=en_US.UTF-8
ENV LC_ALL=en_US.UTF-8

# Copy entrypoint script
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 1433

# Set the entrypoint
ENTRYPOINT ["/entrypoint.sh"]