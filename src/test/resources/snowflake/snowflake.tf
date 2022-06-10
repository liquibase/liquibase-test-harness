provider "snowflake" {
  alias = "sys_admin"
  role  = "SYSADMIN"
}

provider "snowflake" {
  alias = "security_admin"
  role  = "SECURITYADMIN"
}

resource "snowflake_role" "role" {
  provider = snowflake.security_admin
  name     = "TEST_HARNESS_SVC_ROLE"
}

resource "snowflake_database" "db" {
  provider = snowflake.sys_admin
  name     = "LBCAT"
}
resource "snowflake_database_grant" "grant" {
  provider          = snowflake.security_admin
  database_name     = snowflake_database.db.name
  privilege         = "USAGE"
  roles             = [snowflake_role.role.name]
  with_grant_option = false
}

resource "snowflake_warehouse" "warehouse" {
  provider       = snowflake.sys_admin
  name           = "TH_WAREHOUSE"
  warehouse_size = "x-small"
  auto_suspend   = 60
}

resource "snowflake_warehouse_grant" "grant" {
  provider          = snowflake.security_admin
  warehouse_name    = snowflake_warehouse.warehouse.name
  privilege         = "USAGE"
  roles             = [snowflake_role.role.name]
  with_grant_option = false
}

resource "snowflake_schema_grant" "grant" {
  provider          = snowflake.security_admin
  database_name     = snowflake_database.db.name
  schema_name       = "PUBLIC"
  privilege         = "USAGE"
  roles             = [snowflake_role.role.name]
  with_grant_option = false
}

resource "snowflake_schema_grant" "create_table" {
  provider          = snowflake.security_admin
  database_name     = snowflake_database.db.name
  schema_name       = "PUBLIC"
  privilege         = "CREATE TABLE"
  roles             = [snowflake_role.role.name]
  with_grant_option = false
}

resource "snowflake_schema_grant" "create_procedure" {
  provider          = snowflake.security_admin
  database_name     = snowflake_database.db.name
  schema_name       = "PUBLIC"
  privilege         = "CREATE PROCEDURE"
  roles             = [snowflake_role.role.name]
  with_grant_option = false
}

resource "snowflake_schema_grant" "create_sequence" {
  provider          = snowflake.security_admin
  database_name     = snowflake_database.db.name
  schema_name       = "PUBLIC"
  privilege         = "CREATE SEQUENCE"
  roles             = [snowflake_role.role.name]
  with_grant_option = false
}

resource "snowflake_schema_grant" "create_view" {
  provider          = snowflake.security_admin
  database_name     = snowflake_database.db.name
  schema_name       = "PUBLIC"
  privilege         = "CREATE VIEW"
  roles             = [snowflake_role.role.name]
  with_grant_option = false
}

resource "snowflake_user" "user" {
  provider          = snowflake.security_admin
  name              = var.username
  password          = random_password.password.result
  default_role      = snowflake_role.role.name
  default_namespace = "${snowflake_database.db.name}.PUBLIC"
  default_warehouse = snowflake_warehouse.warehouse.name
}

resource "snowflake_role_grants" "grants" {
  provider  = snowflake.security_admin
  role_name = snowflake_role.role.name
  users     = [snowflake_user.user.name]
}

resource "random_password" "password" {
  length  = 16
  special = true
}

output "username" {
  value = snowflake_user.user.name
}

output "password" {
  value     = snowflake_user.user.password
  sensitive = true
}
