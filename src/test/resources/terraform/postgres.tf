# Versions of Postgres to create. 
variable "postgresVersion" {
  type        = list(string)
  description = "Postgres Database Engine Version (example: 9.6, 10, 11)"
  default     = ["9.6", "10", "11", "12", "13"]
}

# Create the security group granting access to the database with a source of the public IP of the runner
module "db_postgres_sg" {
  source = "terraform-aws-modules/security-group/aws//modules/postgresql"

  name        = "postgres db"
  description = "Security group for postgres database with port 5432 open to the runner of this plan"
  vpc_id      = module.vpc.vpc_id

  ingress_cidr_blocks = ["0.0.0.0/0"]
}

# Create the Postgres RDS Databases 
module "postgres" {
  source  = "terraform-aws-modules/rds/aws"
  version = "~> 3.0"
  count   = length(var.postgresVersion)

  identifier = replace("postgres-${var.postgresVersion[count.index]}", ".", "-") // dots not permitted in identifiers, so replace with hyphen

  engine                    = "postgres"
  name                      = "lbcat"
  family                    = "postgres${var.postgresVersion[count.index]}"
  major_engine_version      = var.postgresVersion[count.index]
  engine_version            = var.postgresVersion[count.index]
  instance_class            = "db.t3.micro"
  allocated_storage         = 5
  publicly_accessible       = true
  skip_final_snapshot       = true
  username                  = "lbuser"
  password                  = "LbRootPass1"
  subnet_ids                = module.vpc.public_subnets
  vpc_security_group_ids    = [module.db_postgres_sg.security_group_id]
  create_db_parameter_group = false

}

# Output endpoint (host:port)
output "postgresEndpoint" {
  value = {
    for endpoint in module.postgres :
    endpoint.db_instance_id => endpoint.db_instance_endpoint
  }
}
