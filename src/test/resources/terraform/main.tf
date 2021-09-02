# Get Public IP of the host running the Terraform Plan to grant access to the database
data "http" "ip_address" {
  url = "https://api.ipify.org"
  request_headers = {
    Accept = "text/plain"
  }
}
output "my_public_ip" {
  value = data.http.ip_address.body
}

# Create the security group granting access to the database with a source of the public IP of the runner
module "db_postgres_sg" {
  source = "terraform-aws-modules/security-group/aws//modules/postgresql"

  name        = "postgres db"
  description = "Security group for postgres database with port 5432 open to the runner of this plan"
  vpc_id      = module.vpc.vpc_id

  ingress_cidr_blocks = ["${data.http.ip_address.body}/32"]
}

# VPC with only public subnets for the test databases
module "vpc" {
  source = "terraform-aws-modules/vpc/aws"

  name = "test-harness" //TODO - add dynamic identifier to allow parallel runs
  cidr = "10.0.0.0/16"

  azs                  = ["us-east-1a", "us-east-1b"]
  public_subnets       = ["10.0.101.0/24", "10.0.102.0/24"]
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Terraform   = "true"
    Environment = "dev"
  }
}

# Create the Postgres RDS Databases 
module "db" {
  source  = "terraform-aws-modules/rds/aws"
  version = "~> 3.0"

  identifier = "test-harness" //TODO - add dynamic identifier to allow parallel runs

  engine               = "postgres"
  name                 = "lbcat"
  family               = "postgres11" //TODO - variable here to support multiple versions
  major_engine_version = "11"         //TODO - variable here to support multiple versions
  engine_version       = "11"         //TODO - variable here to support multiple versions
  instance_class       = "db.t3.micro"
  allocated_storage    = 5
  publicly_accessible  = true
  skip_final_snapshot  = true
  username             = "lbuser"
  password             = "LbRootPass1" //TODO - potentially use random password here instead
  #storage_encrypted   = true //TODO - determine if we want to encrypt these test databases
  subnet_ids             = module.vpc.public_subnets
  vpc_security_group_ids = [module.db_postgres_sg.security_group_id]
}

# Output the dbHost URL to use in the harness-config.yml
output "dbHost" {
  value = module.db.db_instance_endpoint
}
