# The below workspace is used by Liquibase.  You will need to update this to work with your organization.
terraform {
  backend "remote" {
    organization = "liquibase"

    workspaces {
      name = "test-harness"
    }
  }
}
