# Public IP to be granted access to the DBs
variable "public_ip" {
  type        = string
  description = "Public IP Address to be granted access to database"
  default     = "0.0.0.0"
}