# Public IP to be granted access to the DB //TODO
variable "public_ip" {
  type        = string
  description = "Public IP Address to be granted access to database"
}

# Versions of Postgres to create
variable "postgresVersions" {
  type        = list(string)
  description = "Postgres Database Engine Versions (example: 11, 11.10, 10)"
}

# Create the security group granting access to the database with a source of the public IP of the runner
module "db_postgres_sg" {
  source = "terraform-aws-modules/security-group/aws//modules/postgresql"

  name        = "postgres db"
  description = "Security group for postgres database with port 5432 open to the runner of this plan"
  vpc_id      = module.vpc.vpc_id

  #ingress_cidr_blocks = ["${var.public_ip}/32"] //TODO: Allow only the runner to access the DB
  ingress_cidr_blocks = ["0.0.0.0/0"]
}

# Create the Postgres RDS Databases 
module "postgres" {
  source  = "terraform-aws-modules/rds/aws"
  version = "~> 3.0"
  count   = length(var.postgresVersions)

  identifier = "postgres${var.postgresVersions[count.index]}"

  engine               = "postgres"
  name                 = "lbcat"
  family               = "postgres${var.postgresVersions[count.index]}"
  major_engine_version = var.postgresVersions[count.index]
  engine_version       = var.postgresVersions[count.index]
  instance_class       = "db.t3.micro"
  allocated_storage    = 5
  publicly_accessible  = true
  skip_final_snapshot  = true
  username             = "lbuser"
  password             = "LbRootPass1"
  #storage_encrypted   = true //TODO - determine if we want to encrypt these test databases
  subnet_ids             = module.vpc.public_subnets
  vpc_security_group_ids = [module.db_postgres_sg.security_group_id]
}

# Output endpoint (host:port)
output "dbEndpoint" {
  value = module.postgres.*.db_instance_endpoint
}
