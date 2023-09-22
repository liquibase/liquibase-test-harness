

# Store remote state in Terraform Cloud.  See Jake Newton for account access.
# Store state information in the "liquibase-dev" workspace.
# These are considered dev resources.

terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.17.0"
    }
    github = {
      source  = "integrations/github"
      version = "5.37.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "3.3.1"
    }
  }
}

provider "aws" {
  region = "us-east-1"
}

provider "github" {
  owner = "liquibase"
}