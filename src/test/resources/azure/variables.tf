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
  default     = "azuretestharness"
  description = "The resource group the sql server should be placed in. "
}

variable "server_name" {
  default     = "aztestharness"
  description = "Name of the server. "
}

variable "database_name" {
  description = "Name on the initial database on the server. "
  default     = "lbcat"
}

variable "admin_login_name" {
  default     = "lbadmin"
  description = "Login name for the sql server administrator. If not set the default login name will be 'lbadmin'."
}

