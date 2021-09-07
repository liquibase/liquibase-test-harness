# Get Public IP of the host running the Terraform Plan to grant access to the database
# output "my_public_ip" {
#   value = data.http.ip_address.body
# }

# Output the dbHost URL to use in the harness-config.yml
output "dbHost" {
  value = module.db.db_instance_endpoint
}
