# Outputs to use in the harness-config.yml
output "dbEndpoint" {
  value = module.db.db_instance_endpoint
}
output "dbUsername" {
  value = module.db.db_instance_instance_username
}
output "dbPassword" {
  value = module.db.db_instance_master_password
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
