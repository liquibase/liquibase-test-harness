
# Output endpoint (fqdn)
output "azureSqlDbEndpoint" {
  description = "FQDN of mssql server created"
  value       = "jdbc:sqlserver://${azurerm_mssql_server.sql_server.fully_qualified_domain_name}:1433;trustServerCertificate=true;databaseName=${azurerm_mssql_database.test.name}"
}

output "admin_user" {
  description = "Admin username"
  value       = var.admin_login_name
}
output "admin_password" {
  description = "Password for admin on the sql server."
  value       = random_string.password.result
  sensitive   = true
}
