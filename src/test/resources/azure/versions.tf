
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
      version = "~>2.0"
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

# This block is needed to fix "insufficient feature blocks" error!
provider "azurerm" {
  features {}
}
