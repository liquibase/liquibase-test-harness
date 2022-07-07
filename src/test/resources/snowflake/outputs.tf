output "username" {
  value = snowflake_user.user.name
}

output "password" {
  value     = snowflake_user.user.password
  sensitive = true
}
