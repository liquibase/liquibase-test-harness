data "http" "ip_address" {
  url = "https://api.ipify.org"
  request_headers = {
    Accept = "text/plain"
  }
}
output "my_public_ip" {
  value = data.http.ip_address.body
}

module "db_postgres_sg" {
  source = "terraform-aws-modules/security-group/aws//modules/postgresql"

  name        = "postgres db"
  description = "Security group for postgres database with port 5432 open to the runner of this plan"
  vpc_id      = module.vpc.vpc_id

  ingress_cidr_blocks = ["${data.http.ip_address.body}/32"]
}

module "vpc" {
  source = "terraform-aws-modules/vpc/aws"

  name = "test-harness"
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

module "db" {
  source  = "terraform-aws-modules/rds/aws"
  version = "~> 3.0"

  identifier = "test-harness"

  engine               = "postgres"
  name                 = "lbcat"
  family               = "postgres11"
  major_engine_version = "11"
  engine_version       = "11"
  instance_class       = "db.t3.micro"
  allocated_storage    = 5
  publicly_accessible  = true
  skip_final_snapshot  = true
  username             = "lbuser"
  password             = "LbRootPass1"
  #storage_encrypted   = true
  subnet_ids             = module.vpc.public_subnets
  vpc_security_group_ids = [module.db_postgres_sg.security_group_id]
}

output "dbHost" {
  value = module.db.db_instance_endpoint
}
