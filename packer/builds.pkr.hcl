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

  provisioner "shell" {
    inline_shebang  = "/bin/bash" # use bash
    execute_command = "sudo bash '{{ .Path }}'"
    script          = "${path.root}/../scripts/setup.sh"
  }
}
