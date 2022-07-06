data "azurerm_client_config" "current" {}

# use pre-created resource group 
# resource "azurerm_resource_group" "test_harness" {
# name     = var.resource_group_name
# location = var.location
# }

resource "azurerm_mssql_server" "sql_server" {
  name                = var.server_name
  resource_group_name = var.resource_group_name
  location            = var.location
  version             = "12.0"
  administrator_login = var.admin_login_name
  administrator_login_password = random_string.password.result
}

resource "azurerm_mssql_firewall_rule" "sql_firewall" {
  count = var.allow_azure_ip_access ? 1 : 0

  name                = "AllowAccessToAzure"
  server_id         = azurerm_mssql_server.sql_server.id  
  start_ip_address    = "0.0.0.0"
  end_ip_address      = "0.0.0.0"
}

resource "azurerm_mssql_database" "test" {
  name      = var.database_name
  server_id = azurerm_mssql_server.sql_server.id
  max_size_gb = 2
  sku_name    = "Basic"

  tags = {
    terraform = "true"
  }
}

resource "azurerm_management_lock" "resource-CanNotDelete-lock" {
  count = var.lock_database_resource == true ? 1 : 0

  name       = "sql-database-CanNotDelete-lock"
  scope      = azurerm_mssql_database.test.id
  lock_level = "CanNotDelete"
  notes      = "Locked due to holding critical data."
}

resource "random_string" "password" {
  length           = 32
  special          = true
  override_special = "/@\" "
}


