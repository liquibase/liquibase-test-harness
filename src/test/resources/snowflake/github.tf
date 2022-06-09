provider "github" {
  owner = "liquibase"
}

data "github_repository" "test-harness" {
  full_name = "liquibase/liquibase-test-harness"
}

data "github_actions_public_key" "test-harness-key" {
  repository = data.github_repository.test-harness.name
}

resource "github_actions_secret" "th_password" {
  repository      = data.github_repository.test-harness.name
  secret_name     = "th_password"
  encrypted_value = "test"
}
