# Outputs to use in the harness-config.yml
output "dbEndpoint" {
  value = module.db.db_instance_endpoint
}
output "dbUsername" {
  value = module.db.db_instance_username
  sensitive = true
}
output "dbPassword" {
  value = module.db.db_instance_password
  sensitive = true
}
output "dbPort" {
  value = module.db.db_instance_port
}
output "dbName" {
  value = module.db.db_instance_name
}
output "dbAddress" {
  value = module.db.db_instance_address
}
