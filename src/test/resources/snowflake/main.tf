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
      version = "4.26.0"
    }
  }
}
