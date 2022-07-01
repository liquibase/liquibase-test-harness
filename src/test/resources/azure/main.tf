data "azurerm_client_config" "current" {}

resource "azurerm_mssql_server" "sql_server" {
  name                = var.server_name
  resource_group_name = var.resource_group_name
  location            = var.location
  version             = "12.0"
  administrator_login = var.admin_login_name
  #administrator_login_password = random_string.password.result
  administrator_login_password = "Run2Trouble!"

}

resource "azurerm_sql_firewall_rule" "sql_firewall" {
  count = var.allow_azure_ip_access ? 1 : 0

  name                = "AllowAccessToAzure"
  resource_group_name = var.resource_group_name
  server_name         = azurerm_mssql_server.sql_server.name
  start_ip_address    = "0.0.0.0"
  end_ip_address      = "0.0.0.0"
}

resource "azurerm_sql_active_directory_administrator" "sql_admin" {
  server_name         = azurerm_mssql_server.sql_server.name
  resource_group_name = var.resource_group_name
  login               = var.ad_admin_login_name
  tenant_id           = data.azurerm_client_config.current.tenant_id
  object_id           = var.ad_admin_object_id
}

resource "azurerm_mssql_database" "test" {
  name      = var.database_name
  server_id = azurerm_mssql_server.sql_server.id
  #collation      = "SQL_Latin1_General_CP1_CI_AS"
  #license_type   = "LicenseIncluded"
  max_size_gb = 2
  sku_name    = "Basic"

  # extended_auditing_policy {
  #   storage_endpoint                        = azurerm_storage_account.example.primary_blob_endpoint
  #   storage_account_access_key              = azurerm_storage_account.example.primary_access_key
  #   storage_account_access_key_is_secondary = true
  #   retention_in_days                       = 6
  # }


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


