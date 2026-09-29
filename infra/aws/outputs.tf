output "instance_id" {
  value = aws_instance.backend.id
}

output "elastic_ip" {
  description = "Point your DNS A record for the API domain at this IP"
  value       = aws_eip.backend.public_ip
}

output "ssh_command" {
  value = "ssh ubuntu@${aws_eip.backend.public_ip}"
}
