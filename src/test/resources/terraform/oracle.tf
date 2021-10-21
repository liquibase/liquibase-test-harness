# Versions of oracle to create. 
variable "oracleVersion" {
  type        = list(string)
  description = "Oracle Database Engine Version (example: 12.1, 12.2, 19)"
  default     = ["12.1", "12.2", "19"]
}

# Create the security group granting access to the database with a source of the public IP of the runner
module "db_oracle_sg" {
  source = "terraform-aws-modules/security-group/aws//modules/oracle-db"

  name        = "oracle db"
  description = "Security group for oracle database with port 1521 open to the runner of this plan"
  vpc_id      = module.vpc.vpc_id

  ingress_cidr_blocks = ["0.0.0.0/0"]
}

# Create the oracle RDS Databases 
module "oracle" {
  source  = "terraform-aws-modules/rds/aws"
  version = "~> 3.0"
  count   = length(var.oracleVersion)

  identifier = replace("oracle-${var.oracleVersion[count.index]}", ".", "-") // dots not permitted in identifiers, so replace with hyphen

  engine                    = "oracle-ee"
  name                      = "LBCAT"
  family                    = "oracle${var.oracleVersion[count.index]}"
  major_engine_version      = var.oracleVersion[count.index]
  engine_version            = var.oracleVersion[count.index]
  instance_class            = "db.t3.small"
  allocated_storage         = 10
  publicly_accessible       = true
  skip_final_snapshot       = true
  username                  = "LBUSER"
  password                  = "LbRootPass1"
  subnet_ids                = module.vpc.public_subnets
  vpc_security_group_ids    = [module.db_oracle_sg.security_group_id]
  create_db_parameter_group = false

}

# Output endpoint (host:port)
output "oracleEndpoint" {
  value = {
    for endpoint in module.oracle :
    endpoint.db_instance_id => endpoint.db_instance_endpoint
  }
}
