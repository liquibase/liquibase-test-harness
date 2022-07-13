data "azurerm_client_config" "current" {}

resource "azurerm_resource_group" "testharness" {
 name     = var.resource_group_name
 location = var.location
 timeouts {
   create = "10m"
   delete = "30m"
 }
}

resource "azurerm_mssql_server" "sql_server" {
  name                = var.server_name
  resource_group_name = var.resource_group_name
  location            = var.location
  version             = "12.0"
  administrator_login = var.admin_login_name
  administrator_login_password = random_string.password.result

  timeouts {
    create = "4h"
    delete = "4h"
  }
  depends_on          = [azurerm_resource_group.testharness]
}

resource "azurerm_mssql_firewall_rule" "sql_firewall" {
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

resource "random_string" "password" {
  length           = 32
  special          = false
}


