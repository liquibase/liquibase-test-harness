# Public IP to be granted access to the DBs - NOT CURRENTLY IN USE
variable "public_ip" {
  type        = string
  description = "Public IP Address to be granted access to database"
  default     = "0.0.0.0"
}
