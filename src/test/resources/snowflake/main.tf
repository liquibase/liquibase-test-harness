terraform {
  cloud {
    organization = "liquibase"
    workspaces {
      name = "test-harness-snowflake"
    }
  }
  required_providers {
    snowflake = {
      source  = "Snowflake-Labs/snowflake"
      version = "0.34.0"
    }
  }
}

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

resource "snowflake_user" "user" {
  provider          = snowflake.security_admin
  name              = "test_harness_user"
  password          = "j^9wr+ccMB@6;%ky"
  default_role      = snowflake_role.role.name
  default_namespace = "${snowflake_database.db.name}.public"
}

resource "snowflake_role_grants" "grants" {
  provider  = snowflake.security_admin
  role_name = snowflake_role.role.name
  users     = [snowflake_user.user.name]
}
