build {
  name    = "csye6225-webapp-image"
  sources = ["source.amazon-ebs.ubuntu"]

  provisioner "file" {
    source = "./scripts/webapp.zip"  # runner
    destination = "${var.shell_env.app_archive_path}/webapp.zip"  # EC2
  }

  provisioner "shell" {
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

      # run-time (app.env)
      "R_DB_HOST=${var.web_env.db_host}",
      "R_DB_PORT=${var.web_env.db_port}",
      "R_DB_CONN_TIMEOUT_MS=${var.web_env.db_conn_timeout_ms}",
      "R_SERVER_PORT=${var.web_env.server_port}",
      "R_API_BASE=${var.web_env.api_base}",
    ]

    inline = [
      "set -euo pipefail",
      "umask 0077",
      "echo '[INFO] Generating build-time .env ...'",
      "cat > /tmp/.env <<EOT",
      "DB_TYPE=$B_DB_TYPE",
      "DB_NAME=$B_DB_NAME",
      "DB_USER=$B_DB_USER",
      "DB_PASS=$B_DB_PASS",
      "APP_GROUP=csye6225",
      "APP_USER=csyeapp",
      "APP_DIR=$B_APP_DIR",
      "APP_ENV_FILE=$B_APP_ENV_FILE",
      "APP_ARCHIVE_PATH=$B_APP_ARCHIVE_PATH",
      "EOT",
      "echo '[INFO] Generating run-time app.env ...'",
      "sudo mkdir -p \"$B_APP_DIR\"",
      "cat > /tmp/app.env <<EOT",
      "DB_HOST=$R_DB_HOST",
      "DB_PORT=$R_DB_PORT",
      "DB_NAME=$B_DB_NAME",
      "DB_USERNAME=$B_DB_USER",
      "DB_PASSWORD=$B_DB_PASS",
      "DB_CONN_TIMEOUT_MS=$R_DB_CONN_TIMEOUT_MS",
      "SERVER_PORT=$R_SERVER_PORT",
      "API_BASE=$R_API_BASE",
      "EOT",
      "echo '[INFO] All env files generated successfully in /tmp.'"
    ]
  }

  provisioner "shell" {
    script = "${path.root}/../scripts/setup.sh"
  }
}
