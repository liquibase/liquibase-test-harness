terraform {
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

resource "snowflake_database" "db" {
  provider = snowflake.sys_admin
  name     = "TEST_HARNESS"
}

# resource "snowflake_warehouse" "warehouse" {
#   provider       = snowflake.sys_admin
#   name           = "TEST_HARNESS"
#   warehouse_size = "large"

#   auto_suspend = 60
# }
