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

module "test-automation" {
  source                 = "app.terraform.io/liquibase/test-automation/aws"
  version                = "0.1.0"
  create_aurora_mysql    = true
  create_aurora_postgres = true
}