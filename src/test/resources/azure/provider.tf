
# Azure Provider for VSPS
provider "azurerm" {
  alias           = "vsps"
  subscription_id = "a38e082e-9dfc-49dd-a5e8-9d13e908b010"
  tenant_id       = "fd6a63a1-3f33-4f34-802a-06ae5ad2217a"
  client_id       = var.vsps_client_id
  client_secret   = var.vsps_client_secret
  features {
    key_vault {
      purge_soft_delete_on_destroy = true
    }
  }
}
# Azure AD Provider
provider "azuread" {
  tenant_id     = "fd6a63a1-3f33-4f34-802a-06ae5ad2217a"
  client_id     = var.vsps_client_id
  client_secret = var.vsps_client_secret
}
