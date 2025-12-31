# Project-Local Maven Repository

This folder contains JDBC drivers that are not available in Maven Central.
Both drivers use `<scope>test</scope>` to ensure they are NOT redistributed in the published artifact.

## Sybase jConnect Driver (jconn4)

**Location:** `com/sybase/jdbc4/jdbc/sybdriver/sybase/1.0/`

The Sybase jConnect JDBC driver is proprietary software from SAP.

### Obtaining the Driver
- Download from [SAP Software Downloads](https://support.sap.com/swdc) (requires SAP account)
- Or obtain from the SAP ASE SDK

## EDB JDBC Driver (edb-jdbc18)

**Location:** `com/edb/edb-jdbc18/42.3.2.1/`

The EnterpriseDB JDBC driver for EDB Postgres Advanced Server.

Note: Newer versions are available on Maven Central (`com.enterprisedb:edb-jdbc`), but they have
a ServiceLoader initialization conflict with the Sybase driver. This older version avoids that issue.

### Obtaining the Driver
- Download from [EDB Software Downloads](https://www.enterprisedb.com/software-downloads-postgres)
- Or obtain from EDB Postgres Advanced Server installation (`/usr/edb/jdbc/edb-jdbc18.jar`)

## License Note

Both drivers are proprietary software. Their inclusion here with `test` scope means they are
only used during test execution and are NOT redistributed as part of the liquibase-test-harness artifact.
