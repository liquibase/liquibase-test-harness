# Versions of Postgresql to create. 
variable "postgresqlVersion" {
  type        = list(string)
  description = "Postgresql Database Engine Version (example: 9.6, 10, 11)"
  default     = ["9.6", "10", "11", "12", "13"]
}

# Create the security group granting access to the database with a source of the public IP of the runner
module "db_postgresql_sg" {
  source = "terraform-aws-modules/security-group/aws//modules/postgresql"

  name        = "postgresql db"
  description = "Security group for postgresql database with port 5432 open to the runner of this plan"
  vpc_id      = module.vpc.vpc_id

  ingress_cidr_blocks = ["0.0.0.0/0"]
}

# Create the Postgresql RDS Databases 
module "postgresql" {
  source  = "terraform-aws-modules/rds/aws"
  version = "~> 3.0"
  count   = length(var.postgresqlVersion)

  identifier = replace("postgresql-${var.postgresqlVersion[count.index]}", ".", "-") // dots not permitted in identifiers, so replace with hyphen

  engine                    = "postgres"
  name                      = "lbcat"
  family                    = "postgres${var.postgresqlVersion[count.index]}"
  major_engine_version      = var.postgresqlVersion[count.index]
  engine_version            = var.postgresqlVersion[count.index]
  instance_class            = "db.t3.micro"
  allocated_storage         = 5
  publicly_accessible       = true
  skip_final_snapshot       = true
  username                  = "lbuser"
  password                  = "LbRootPass1"
  subnet_ids                = module.vpc.public_subnets
  vpc_security_group_ids    = [module.db_postgresql_sg.security_group_id]
  create_db_parameter_group = false

}

# Output endpoint (host:port)
output "postgresqlEndpoint" {
  value = {
    for endpoint in module.postgresql :
    endpoint.db_instance_id => endpoint.db_instance_endpoint
  }
}
