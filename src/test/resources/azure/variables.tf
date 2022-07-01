variable "vsps_client_id" {
  description = "Azure VSPS Client ID"
}
variable "vsps_client_secret" {
  description = "Azure VSPS Client ID"
}

variable "location" {
  default     = "South Central US"
  description = "The location where the resources should be created."
}

variable "resource_group_name" {
  default     = "TestHarness"
  description = "The resource group the sql server should be placed in. "
}

variable "server_name" {
  description = "Name of the server. "
}

variable "database_name" {
  description = "Name on the initial database on the server. "
}

variable "admin_login_name" {
  default     = "lbadmin"
  description = "Login name for the sql server administrator. If not set the default login name will be 'lbadmin'."
}

variable "allow_azure_ip_access" {
  default     = "false"
  description = "If azure ip ranges should be allowed through the sql server firewall."
}

variable "ad_admin_login_name" {
  default     = "ad_lbadmin"
  description = "Name of the login for sql admin loging in from Azure AD. "
}

variable "ad_admin_object_id" {
  description = "Object ID of the login for the sql admin"
}

variable "lock_database_resource" {
  default     = "false"
  description = "Param defining whether to set CanNotDelete lock on the database resource upon DB creation. Possible input values is 'true' and 'false'."
}
