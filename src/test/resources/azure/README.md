# terraform-azurerm-sql-server

## Creates a SQL server w/ DB
Terraform module for Azure where it creates a SQL server with initial database and Azure AD login. 

A SQL server has to have a sql administator login, so this will be generated in the package. It generates a 32 character long random password for the login. This password is outputed and stored in the state file, so make sure your state file is secure. Read more about sensitive data in state here: https://www.terraform.io/docs/state/sensitive-data.html

Installs following resources:
- SQL Server
- SQL Database
- Active Directory Administrator

Optionally installs following: 
- Firewall rule for azure ip ranges. 

## Usage

```hcl

resource "azurerm_resource_group" "storage_rg" {
  name     = "storage-rg"
  location = "westeurope"
}

module "sql_server" {
  source = "innovationnorway/sql-server/azurerm"

  server_name           = "weather-logs"
  database_name         = "temperature"
  allow_azure_ip_access = true

  resource_group_name = "${azurerm_resource_group.storage_rg.name}"
  location            = "${azurerm_resource_group.image_resizer.location}"
  environment         = "lab"
  release             = "release 2018-07-21.001"

  # SQL login admin
  admin_login_name = "super_awesome_admin_username"

  # AAD login admin
  ad_admin_login_name = "cute-kitten-57"
  ad_admin_tenant_id  = "2b25609c-e6e8-4f24-b7be-aa9fdef90a2d"
  ad_admin_object_id  = "18821d6f-adbe-4d4d-97ca-71a56f70e392"

  # Scaling
  database_edition                          = "Standard"
  database_requested_service_objective_name = "S3"

  database_collation = "SQL_LATIN1_GENERAL_CP1_CI_AS"

  tags {
      a       = "b",
      project = "weather-logs"
  }
}

```