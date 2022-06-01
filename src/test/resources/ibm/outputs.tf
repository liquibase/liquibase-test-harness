output "ibm_is_floating_ip" {
  value       = ibm_is_floating_ip.testacc_floatingip.address
  description = "The public floating IP of the server instance"
}
