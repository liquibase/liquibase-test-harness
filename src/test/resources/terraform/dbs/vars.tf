# Tenancy variables
variable region { default = "us-phoenix-1" }
variable tenancy_ocid { default = "ocid1.tenancy.oc1..aaaaaaaadf5ufvefml4kawet7knxkwepvpmovvg7k3hc666fvwp3s7vkyv6q" }
variable compartment_ocid { default = "ocid1.compartment.oc1..aaaaaaaaho47xcbfey4ghnicj43bjib75wvoni2ym4sbd7u7tw62tceqy5hq" }
variable subnet_id { default = "ocid1.subnet.oc1.phx.aaaaaaaapthq4y2ac7as2azgjjdwwjnxizd5wtg6r5bry7mlbcuzjm7hn2uq" }
variable ssh_public_key { default = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDfj5XSVD2U6D0FVV6uHKoZ7VsBvcn++phi3NCuC6+ApFUIHHLWMZ4AUz+q72roW+QcYy1HUiZc9dbndcgW7SegslLc8ZsMMIM1Xm94eINSn8p0bnsP9f2mDgmFmwjbanTWyif3KftigmHYPF9GPEJKbPfRRbklP7xxYjLyeAN12V30CPSW+CS5VJ6E9i7jSrh5MnMXTDs2R7IuCkJbp1/a7Bix8RH7+WE1IoPE5DV1fgVgicwXRQbmVmyABX/nEjTQpMRRFYl6mfSdQF8nDxtX9p/tC3MjEpvihX5hxKo6ZBGashAc/aNBd1Oay6z486Boh+OwHOyGjUiUy0NYOcch r2@LAPTOP-4648ND7R" }
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
variable time_zone { default = "America/Chicago" }
