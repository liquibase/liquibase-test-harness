
# Output endpoint (fqdn)
output "azureSqlDbEndpoint" {
  value = {
    description ="FQDN of mssql server created"
    value       = azurerm_mssql_server.sql_server.fully_qualified_domain_name
  }
}

output "admin_password" {
  description = "Password for admin on the sql server."
  value       = random_string.password.result
  sensitive   = true
}


