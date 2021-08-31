terraform {
  backend "remote" {
    organization = "liquibase"

    workspaces {
      name = "test-harness"
    }
  }
}
