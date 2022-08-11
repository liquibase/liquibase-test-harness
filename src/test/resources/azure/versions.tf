
terraform {
  cloud {
    organization = "liquibase"
    workspaces {
      name = "test-harness-azure"
    }
  }
  required_providers {
    azurerm = {
      source = "hashicorp/azurerm"
      version = "~>3.11.0"
    }
    azuread = {
      source = "hashicorp/azuread"
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
