# Tenancy variables
variable region { default = "us-phoenix-1" }
variable compartment_ocid { default = "ocid1.compartment.oc1..aaaaaaaa2msdfh2g3n4s6fadsvl2thelw5gtnwhbcsqwcgfg2hcsk7mjc3ya" }
variable subnet_id { default = "ocid1.subnet.oc1.phx.aaaaaaaaypsgfwx3tnkgweu2e5djazooagapt36pj6hfl7pdhpanaqtw2v4a" }
variable ssh_public_key { default = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC96kMjy8kLiU3PvUqn/PlFpVGq9VyeiFNphp3u3wf+JJUlxlh4USLC8KM2X2ZF5630s3hXoFMC2fE+7GtAAxdCAM0WkekJO0NO59fMMKy2/ZcKMxUANhw3nTo/1HUF5VOrWMOSNaLBP7O3WOq7tfs6vNbyM9wcXQmrOfKS0iKkKe+O5Q1K0RGKpuW48xUmLTCXeaX7t6ovZpXuTKj8QtSxRTuMUolQyJqr05fdux19lt3m9cnzv2TS/cYVCKOjp715A3mONOJi7izkiTQyggEIZpeQ9ohu6yXOzSUUpHifCyPNvjjt92dRR4es2dp9Xjnn09bYSDltMgA8D/vzQckn" }
# DBS variables
variable db_admin_pass { default = "JamesBond007_123#" }
variable database_edition { default = "ENTERPRISE_EDITION_EXTREME_PERFORMANCE" }
variable db_version { default = "19.9.0.0" }
variable cpu_core_count { default = "1" }
variable data_storage_percentage { default = "80" }
variable data_storage_size_in_gb { default = "256" }
variable db_hostname { default = "rddbs" }
variable license_model { default = "LICENSE_INCLUDED" }
variable node_count { default = "1" }
variable shape { default = "VM.Standard2.4" }
variable time_zone { default = "Asia/Calcutta" }
