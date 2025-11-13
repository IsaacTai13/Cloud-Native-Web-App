packer {
  required_version = ">=1.9.0"

  required_plugins {
    amazon = {
      version = ">= 1.3.0"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

source "amazon-ebs" "ubuntu" {
  profile       = var.profile
  region        = var.region
  instance_type = var.instance_type
  ssh_username  = var.ssh_username

  ami_name        = "${var.ami_name}-{{timestamp}}"
  ami_description = "Custom AMI with web app and local DB"
  ami_users       = [var.demo_account_id]

  associate_public_ip_address = true

  source_ami_filter {
    filters = {
      name                = "ubuntu/images/*ubuntu-noble-24.04-amd64-server-*"
      root-device-type    = "ebs"
      virtualization-type = "hvm"
    }
    owners      = ["099720109477"]
    most_recent = true
  }

  tags = {
    Name      = var.ami_name
    ManagedBy = "packer"
  }
}
