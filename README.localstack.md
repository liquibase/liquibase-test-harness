## Localstack

The Liquibase Test Harness repository uses [localstack](https://www.localstack.cloud/) and the [awslocal CLI](https://github.com/localstack/awscli-local) to run tests against different AWS RDS (`MySQL`, `PostgreSQL`, `MariaDB`, and `Microsoft SQL Server`) databases locally. [localstack](https://www.localstack.cloud/) provides a local AWS cloud stack for development and testing purposes, and `awslocal` is a CLI tool for interacting with local AWS resources.

Check out the full `localstack` RDS documentation [here](https://docs.localstack.cloud/user-guide/aws/rds/).

### Prerequisites

1. **Docker**: `localstack` runs in a Docker container. Install Docker by following the instructions [here](https://docs.docker.com/engine/install/)
2. **localstack**: Install `localstack` by following the instructions [here](https://docs.localstack.cloud/getting-started/installation/)
3. `awslocal` CLI: Install the awslocal CLI tool by running the following command: `pip install awscli-local`
4. Creating RDS instances is a `localstack` **PRO** feature so you need a valid `localstack` **PRO API KEY**

### Starting localstack

Before creating local instances and running your tests, start `localstack` by running the following command:

```bash
localstack auth set-token <THE TOKEN IS IN BITWARDEN>
localstack start -d
```

### Creating a local RDS instance

To create a local RDS instance for testing, you can use the `awslocal` CLI. Here's an example command to create an Aurora MySQL instance:

```bash
awslocal rds create-db-cluster --db-cluster-identifier aurora-mysql-primary-cluster --engine aurora-mysql --engine-version 8.0 --database-name test --master-username test --master-user-password test

awslocal rds create-db-instance --db-instance-identifier aurora-mysql-primary-cluster-instance --db-cluster-identifier aurora-mysql-primary-cluster --engine aurora-mysql --db-instance-class db.t3.medium
```

Check the output to see the instance port:

```yaml
DBInstance:
  AllocatedStorage: 200
  AutoMinorVersionUpgrade: false
  AvailabilityZone: us-east-1a
  BackupRetentionPeriod: 1
  CopyTagsToSnapshot: false
  DBInstanceArn: arn:aws:rds:us-east-1:000000000000:db:mssql2019
  DBInstanceClass: db.t3.medium
  DBInstanceIdentifier: mssql2019
  DBInstanceStatus: creating
  DBName: lbcat
  DBParameterGroups:
  - DBParameterGroupName: default.sqlserver-ee
    ParameterApplyStatus: in-sync
  DBSecurityGroups: []
  DbInstancePort: 4510
  DbiResourceId: db-9CB674E4
  DeletionProtection: false
  EnabledCloudwatchLogsExports: []
  Endpoint:
    Address: localhost.localstack.cloud
    Port: 4510
  Engine: sqlserver-ee
  EngineVersion: '2019'
  IAMDatabaseAuthenticationEnabled: false
  InstanceCreateTime: '2024-01-11T06:52:28.087000+00:00'
  LicenseModel: license-included
  MasterUsername: lbuser
  MultiAZ: false
  OptionGroupMemberships:
  - OptionGroupName: None
    Status: in-sync
  PreferredBackupWindow: 13:14-13:44
  PreferredMaintenanceWindow: wed:06:38-wed:07:08
  PubliclyAccessible: false
  ReadReplicaDBInstanceIdentifiers: []
  StatusInfos: []
  StorageEncrypted: false
  StorageType: gp2
  TagList: []
  VpcSecurityGroups:
  - Status: active
    VpcSecurityGroupId: sg-e66159e3935e2e43a
```

### Running Tests

With `localstack` running, execute your tests against the local AWS resources. Make sure your application or test suite is configured to use the local AWS endpoints provided by `localstack` (e.g., `http://localhost:4566`).

You can check the instance port by executing a describe command over the local instance:

```bash
awslocal rds describe-db-instances --db-instance-identifier mssql2019  --query 'DBInstances[0].Endpoint.Port' | jq -r 
```

Check how other local databases platforms are created in the [aws.yml](.github/workflows/aws.yml) GitHub workflow.
