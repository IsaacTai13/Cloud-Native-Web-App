# Render the CloudWatch Agent config on the Packer side (before upload)
locals {
  cwagent_config = templatefile("${path.root}/amazon-cloudwatch-agent.json.tmpl", {
    APP_DIR      = var.shell_env.app_dir
    SERVICE_NAME = var.shell_env.service_name
  })
}

build {
  name    = "csye6225-webapp-image"
  sources = ["source.amazon-ebs.ubuntu"]

  provisioner "file" {
    source      = var.shell_env.app_archive_runner_path          # runner
    destination = "${var.shell_env.app_archive_path}/webapp.zip" # EC2
  }

  provisioner "shell" {
    inline_shebang = "/bin/bash" # use bash

    environment_vars = [
      "B_DB_TYPE=${var.shell_env.db_type}",
      "B_DB_NAME=${var.shell_env.db_name}",
      "B_DB_USER=${var.shell_env.db_user}",
      "B_DB_PASS=${var.shell_env.db_pass}",
      "B_APP_GROUP=${var.shell_env.app_group}",
      "B_APP_USER=${var.shell_env.app_user}",
      "B_APP_DIR=${var.shell_env.app_dir}",
      "B_APP_ENV_FILE=${var.shell_env.app_env_file}",
      "B_APP_ARCHIVE_PATH=${var.shell_env.app_archive_path}",
      "B_SERVICE_NAME=${var.shell_env.service_name}",

      # run-time (app.env)
      "R_DB_HOST=${var.web_env.db_host}",
      "R_DB_PORT=${var.web_env.db_port}",
      "R_DB_CONN_TIMEOUT_MS=${var.web_env.db_conn_timeout_ms}",
      "R_SERVER_PORT=${var.web_env.server_port}",
    ]

    inline = [
      "set -euo pipefail",
      "umask 0077",
      "echo '[INFO] Generating build-time .env ...'",
      <<-EOC
cat > /tmp/.env <<EOT
APP_GROUP=csye6225
APP_USER=csyeapp
APP_DIR=$B_APP_DIR
APP_ENV_FILE=$B_APP_ENV_FILE
APP_ARCHIVE_PATH=$B_APP_ARCHIVE_PATH
SERVICE_NAME=$B_SERVICE_NAME
EOT
EOC
      ,
      "echo '[INFO] Generating run-time app.env ...'",
      "sudo mkdir -p \"$B_APP_DIR\"",
      <<-EOC
cat > /tmp/app.env <<EOT
DB_CONN_TIMEOUT_MS=$R_DB_CONN_TIMEOUT_MS
SERVER_PORT=$R_SERVER_PORT
APP_DIR=$B_APP_DIR
EOT
EOC
      ,
      "echo '[INFO] All env files generated successfully in /tmp.'"
    ]
  }

  provisioner "shell" {
    inline_shebang = "/bin/bash" # use bash
    environment_vars = [
      "B_APP_GROUP=${var.shell_env.app_group}",
      "B_APP_USER=${var.shell_env.app_user}",
      "B_APP_DIR=${var.shell_env.app_dir}",
      "B_SERVICE_NAME=${var.shell_env.service_name}"
    ]

    inline = [
      "echo '[INFO] Generating systemd file...'",
      <<-EOC
sudo tee /etc/systemd/system/$B_SERVICE_NAME.service > /dev/null <<EOT
[Unit]
Description=CSYE6225 Web Application Service
After=network.target

[Service]
Type=simple
User=$B_APP_USER
Group=$B_APP_GROUP
WorkingDirectory=$B_APP_DIR
EnvironmentFile=$B_APP_DIR/.env
ExecStart=/usr/bin/java -jar $B_APP_DIR/webapp.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOT
EOC
      ,
      "sudo chmod 0644 /etc/systemd/system/$B_SERVICE_NAME.service",
      "sudo chown root:root /etc/systemd/system/$B_SERVICE_NAME.service"
    ]
  }

  # ------------------------------------
  # Install and configure Amazon CloudWatch Unified Agent
  # ------------------------------------
  provisioner "shell" {
    inline_shebang = "/bin/bash"
    inline = [
      "set -euo pipefail",
      "echo '[INFO] Installing Amazon CloudWatch Agent...'",

      # Download and install (Ubuntu version)
      "curl -fsSL -o /tmp/amazon-cloudwatch-agent.deb https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb",
      "sudo dpkg -i /tmp/amazon-cloudwatch-agent.deb",

      # Create configuration directory
      "sudo mkdir -p /opt/aws/amazon-cloudwatch-agent/etc",
    ]
  }

  # Write the rendered JSON content into a temporary file on the target instance
  provisioner "shell" {
    inline_shebang = "/bin/bash"
    inline = [
      "set -euo pipefail",
      "echo '[INFO] Generating cloudwatch agent file...'",
      <<-EOC
cat > /tmp/amazon-cloudwatch-agent.json <<'EOF'
${local.cwagent_config}
EOF
EOC
      ,
      "if [ -f /tmp/amazon-cloudwatch-agent.json ]; then",
      "  echo '[SUCCESS] Config file created successfully:'",
      "  head -n 10 /tmp/amazon-cloudwatch-agent.json",
      "else",
      "  echo '[ERROR] Failed to generate /tmp/amazon-cloudwatch-agent.json'; exit 1;",
      "fi"
    ]
  }

  # Move the rendered config file to the official CloudWatch Agent directory
  provisioner "shell" {
    inline_shebang = "/bin/bash" # use bash
    inline = [
      "set -euo pipefail",
      "sudo mv /tmp/amazon-cloudwatch-agent.json /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json",
      "sudo cp /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json.bk",
      "sudo chown root:root /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json",
      "sudo chmod 0644 /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json",
      "sudo systemctl enable amazon-cloudwatch-agent"
    ]
  }

  # ------------------------------------
  # Verify CloudWatch Agent config & ensure log dirs
  # ------------------------------------
  provisioner "shell" {
    inline_shebang = "/bin/bash"
    environment_vars = [
      "B_APP_DIR=${var.shell_env.app_dir}",
      "B_APP_USER=${var.shell_env.app_user}",
      "B_APP_GROUP=${var.shell_env.app_group}"
    ]
    inline = [
      "set -euo pipefail",
      "echo '[INFO] Ensuring log directory exists...'",
      "sudo mkdir -p \"$B_APP_DIR/log\"",

      "echo '[INFO] Starting CloudWatch Agent for config validation...'",
      "sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json -s || true",
      "sudo systemctl enable amazon-cloudwatch-agent",
      "sudo systemctl status amazon-cloudwatch-agent --no-pager || true",
      "echo '[INFO] CloudWatch Agent validated successfully.'"
    ]
  }

  provisioner "shell" {
    inline_shebang  = "/bin/bash" # use bash
    execute_command = "sudo bash '{{ .Path }}'"
    script          = "${path.root}/../scripts/setup.sh"
  }
}
