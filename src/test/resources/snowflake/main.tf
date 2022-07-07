terraform {
  cloud {
    organization = "liquibase"
    workspaces {
      name = "test-harness-snowflake"
    }
  }
  required_providers {
    snowflake = {
      source  = "Snowflake-Labs/snowflake"
      version = "0.34.0"
    }
    github = {
      source  = "integrations/github"
      version = "~> 4.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "3.3.1"
    }
  }
}
