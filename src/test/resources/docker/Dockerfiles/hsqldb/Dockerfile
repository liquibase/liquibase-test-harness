FROM eclipse-temurin:11-jre

# Add build arguments
ARG HSQLDB_USER
ARG HSQLDB_PASSWORD
ARG HSQLDB_VERSION
ARG HSQLDB_DATABASE_ALIAS

# Set environment variables
ENV HSQLDB_VERSION=${HSQLDB_VERSION}
ENV HSQLDB_HOME=/opt/hsqldb

RUN apt-get update && \
    apt-get install -y wget unzip netcat-traditional && \
    rm -rf /var/lib/apt/lists/*

# Download and setup HSQLDB with dynamic version folder
RUN mkdir -p ${HSQLDB_HOME} && \
    HSQLDB_SHORT_VERSION=$(echo ${HSQLDB_VERSION} | cut -d'.' -f1,2) && \
    SHORT_VERSION_UNDERSCORE=$(echo ${HSQLDB_SHORT_VERSION} | tr '.' '_') && \
    wget -O /tmp/hsqldb.zip "https://sourceforge.net/projects/hsqldb/files/hsqldb/hsqldb_${SHORT_VERSION_UNDERSCORE}/hsqldb-${HSQLDB_VERSION}.zip/download" && \
    unzip /tmp/hsqldb.zip -d /tmp && \
    mv /tmp/hsqldb-${HSQLDB_VERSION}/hsqldb/lib/hsqldb.jar ${HSQLDB_HOME}/ && \
    mv /tmp/hsqldb-${HSQLDB_VERSION}/hsqldb/lib/sqltool.jar ${HSQLDB_HOME}/ && \
    rm -rf /tmp/hsqldb*

# Create directory for database files
RUN mkdir -p ${HSQLDB_HOME}/data

# Copy initialization script and startup script
COPY init.sql start.sh ${HSQLDB_HOME}/
RUN chmod +x ${HSQLDB_HOME}/start.sh && \
    sed -i "s/HSQLDB_USER/${HSQLDB_USER}/g" ${HSQLDB_HOME}/init.sql && \
    sed -i "s/HSQLDB_PASSWORD/${HSQLDB_PASSWORD}/g" ${HSQLDB_HOME}/init.sql && \
    sed -i "s/HSQLDB_DATABASE_ALIAS/${HSQLDB_DATABASE_ALIAS}/g" ${HSQLDB_HOME}/start.sh

# Expose the default HSQLDB port
EXPOSE 9001

# Set working directory
WORKDIR ${HSQLDB_HOME}

# Change the CMD to use the startup script
CMD ["./start.sh"]
