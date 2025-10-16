variable "profile" {
  type = string
}

variable "region" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "subnet_id" {
  type = string
}

variable "security_group_id" {
  type = string
}

variable "demo_account_id" {
  type = string
}

variable "instance_type" {
  type    = string
  default = "t3.micro"
}

variable "ssh_username" {
  type    = string
  default = "ubuntu"
}

variable "ami_name" {
  type    = string
  default = "csye6225-app-ubuntu"
}

### ==== ENV ====
variable "shell_env" {
  type = object({
    # Database
    db_type            = string
    db_name            = string
    db_user            = string
    db_pass            = string

    # App identity
    app_group          = string
    app_user           = string
    app_dir            = string
    app_env_file       = string
    app_archive_path   = string
  })
}

variable "web_env" {
  type = object({
    db_host            = string
    db_port            = number
    db_conn_timeout_ms = number
    server_port        = number
    api_base           = string
  })
}
