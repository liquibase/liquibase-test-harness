module "vpc" {
  source = "terraform-aws-modules/vpc/aws"

  name = "test-harness"
  cidr = "10.0.0.0/16"

  azs              = ["us-east-1a", "us-east-1b"]
  private_subnets  = ["10.0.1.0/24", "10.0.2.0/24"]
  public_subnets   = ["10.0.101.0/24", "10.0.102.0/24"]
  database_subnets = ["10.0.201.0/24", "10.0.202.0/24"]

  enable_nat_gateway = true
  enable_vpn_gateway = true

  create_database_subnet_group           = true
  create_database_subnet_route_table     = true
  create_database_internet_gateway_route = true

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
  subnet_ids = module.vpc.database_subnets
}
