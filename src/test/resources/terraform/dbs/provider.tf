terraform {
  required_providers {
    oci = {
      source = "hashicorp/oci"
      #version = "4.14.0"
    }
  }
}

provider "oci" {
  # Configuration options
  #auth = "InstancePrincipal"
  region = var.region
  config_file_profile = "DEFAULT"
  #tenancy_ocid = var.tenancy_ocid
  #user_ocid = var.user_ocid
  #fingerprint = var.fingerprint
  #private_key_path = var.private_key_path
}
