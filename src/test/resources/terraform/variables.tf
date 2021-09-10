# variable "public_ip" {
#   type        = string
#   description = "Public IP Address to be granted access to database"
# }
# variable "dbEngine" {
#   type        = string
#   description = "Database Engine (example: postgres, oracle, mysql)"
# }
# variable "dbName" {
#   type        = string
#   description = "Database Name (example: lbcat)"
#   default     = "lbcat"
# }
# variable "dbFamily" {
#   type        = string
#   description = "Database Parameter Family Group (example: postgres11, mysql5.7)"
# }
# variable "dbEngineMajorVersion" {
#   type        = string
#   description = "Database Engine Option Group Version (example: 11, 5.7)"
# }
# variable "postgresVersions" {
#   type        = list(string)
#   description = "Postgres Database Engine Versions (example: 11, 11.10, 10)"
# }
# variable "dbUsername" {
#   type        = string
#   description = "Database Master Username"
#   default     = "lbuser"
# }
# variable "dbPassword" {
#   type        = string
#   description = "Database Master Password"
#   default     = "LbRootPass1"
# }
# variable "dbInstanceClass" {
#   type        = string
#   description = "Database Instance Class"
#   default     = "db.t3.micro"
# }
