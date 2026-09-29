variable "aws_region" {
  description = "AWS region to deploy into"
  type        = string
  default     = "us-east-1"
}

variable "instance_type" {
  description = "EC2 instance type running the backend + Postgres via Docker Compose"
  type        = string
  default     = "t3.micro"
}

variable "root_volume_gb" {
  description = "Root EBS volume size in GB"
  type        = number
  default     = 20
}

variable "my_ip" {
  description = "Your public IP in CIDR form (e.g. 1.2.3.4/32), allowed to SSH into the instance. Find yours with `curl ifconfig.me`."
  type        = string
}

variable "ssh_public_key_path" {
  description = "Path to the local SSH public key to install on the instance"
  type        = string
  default     = "~/.ssh/id_ed25519.pub"
}

variable "api_domain" {
  description = "Domain name the backend will be served at (Caddy requests a Let's Encrypt cert for this). You must point its DNS A record at the instance's Elastic IP (Terraform output) for the cert to issue."
  type        = string
  default     = "api.divvy.arjavatripathi.me"
}

variable "db_name" {
  description = "Postgres database name"
  type        = string
  default     = "expensetracker"
}

variable "db_user" {
  description = "Postgres username"
  type        = string
  default     = "expensetracker"
}

variable "db_password" {
  description = "Postgres password"
  type        = string
  sensitive   = true
}

variable "jwt_secret" {
  description = "JWT signing secret for the backend"
  type        = string
  sensitive   = true
}

variable "jwt_expiration" {
  description = "JWT expiration in milliseconds"
  type        = string
  default     = "86400000"
}
