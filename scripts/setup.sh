#!/bin/bash

set -euo pipefail

### ====== Configure log color ======
log()  { echo -e "\033[1;32m[OK]\033[0m $*"; }
info() { echo -e "\033[1;34m[INFO]\033[0m $*"; }
err()  { echo -e "\033[1;31m[ERR]\033[0m  $*" >&2; }

require_root() {
  if [[ $EUID -ne 0 ]]; then
    err "Please run this script as root or with sudo."
    exit 1
  fi
}

### ========= Load setup env (.env for this script) ==========
# export all the value in .env to environment
# by turning auto export on and then turn if off
SETUP_ENV_PATH="${SETUP_ENV_PATH:-/tmp/.env}"

if [[ -f "$SETUP_ENV_PATH" ]]; then
  set -a
  . "$SETUP_ENV_PATH"
  set +a
else
  err "Missing "$SETUP_ENV_PATH" file for setup configuration!"
  exit 1
fi

# Require essential vars (no defaults)
require_var() {
  local name="$1"
  # using indirect expansion -> fetch value of variable by name
  if [[ -z "${!name:-}" ]]; then
    err "Missing required env variable: ${name}"
    exit 1
  fi
}

# Mark which are REQUIRED vs OPTIONAL
REQUIRED_VARS=(
  DB_TYPE
  DB_NAME
  DB_USER
  DB_PASS
  APP_GROUP
  APP_USER
  APP_DIR
  APP_ENV_FILE
)
for v in "${REQUIRED_VARS[@]}"; do
  require_var "$v";
done

# Set up major file path: web app zip & .env
APP_ARCHIVE_PATH="${APP_ARCHIVE_PATH:-/tmp}"
APP_ARCHIVE="$(find "$APP_ARCHIVE_PATH" -maxdepth 1 -type f -name '*.zip' -print -quit)"
if [[ -z "${APP_ARCHIVE:-}" ]]; then
  err "No .zip artifact found under $APP_ARCHIVE_PATH"
  exit 1
fi
ENV_FILE="$APP_DIR/.env"

### ====== Copy app.env to APP_DIR as .env ======
# if APP_ENV_FILE is provided, move it under $APP_DIR and rename to .env
copy_app_env() {
  local source_file="${APP_ARCHIVE_PATH}/${APP_ENV_FILE}"

  if [[ ! -f "$source_file" ]]; then
    err "app.env not found at $source_file"
    exit 1
  fi

  info "Creating application directory at $ENV_FILE..."
  mkdir -p "$APP_DIR"

  info "Copying app.env to $ENV_FILE..."
  cp "$source_file" "$ENV_FILE"
  chmod 640 "$ENV_FILE"

  log "app.env copied to $ENV_FILE successfully"
}

### ========= Require .env under $APP_DIR ===========
# Web app need an .env file to run, so check if exist
ensure_env_file() {
  if [[ ! -f "$ENV_FILE" ]]; then
    echo "[ERR] .env not found at $ENV_FILE"
    echo "[INFO] Creating template: $ENV_FILE.example"
    mkdir -p "$APP_DIR"

    cat > "$ENV_FILE.example" <<'ENV'
DB_HOST=
DB_PORT=
DB_NAME=
DB_USERNAME=
DB_PASSWORD=
DB_CONN_TIMEOUT_MS=

SERVER_PORT=8081
API_BASE=http://localhost
# Notes:
# 1) Rename this file to ".env" after filling real values.
# 2) Adjust permissions as needed (e.g., chmod 640 .env)
ENV

    echo
    echo "[INFO] Template created at $ENV_FILE.example"
    echo "Please fill it in, rename to '.env', then rerun this script."
    exit 1
  fi

  echo "[INFO] Loading env from $ENV_FILE"
  set -a
  . "$ENV_FILE"
  set +a
}

update_system() {
  info "Updating package lists..."
  apt-get update -y
  info "Upgrading installed packages..."
  DEBIAN_FRONTEND=noninteractive apt-get upgrade -y
}

install_common_tools() {
  info "Installing common tools (unzip, tar, curl)..."
  apt-get install -y unzip tar curl sudo vim iproute2 >/dev/null
}

install_java() {
  info "Installing OpenJDK 17..."
  apt-get install -y openjdk-17-jdk >/dev/null

  # set JAVA_HOME for Maven
  export JAVA_HOME="$(dirname "$(dirname "$(readlink -f "$(which java)")")")"
  echo "JAVA_HOME=$JAVA_HOME" >> /etc/environment

  log "Java installed successfully at $JAVA_HOME"
}

install_database() {
  case "$DB_TYPE" in
    postgres|postgresql)
      info "Installing PostgreSQL..."
      apt-get install -y postgresql postgresql-contrib >/dev/null

      # Check if systemd exists
      if command -v systemctl >/dev/null 2>&1 && systemctl list-units >/dev/null 2>&1; then
        info "Systemd detected — enabling PostgreSQL service"
        systemctl enable --now postgresql
      else
        info "Systemd not found — starting PostgreSQL manually"
        # Try service or pg_ctlcluster (for non-systemd envs like Docker)
        if command -v service >/dev/null 2>&1; then
          service postgresql start || true
        elif command -v pg_ctlcluster >/dev/null 2>&1; then
          pg_ctlcluster 16 main start || true
        else
          err "Could not start PostgreSQL automatically. Please start manually."
        fi
      fi
      log "PostgreSQL is running"
      ;;
    mysql)
      err "Currently configured for PostgreSQL only"
      exit 1
      ;;
    mariadb)
      err "Currently configured for PostgreSQL only"
      exit 1
      ;;
    *)
      err "Unsupported DB_TYPE"
  esac
}

create_postgres_db() {
  info "Creating PostgreSQL database and user (if not already existing)..."

  # Creating PostgreSQL role and database
  sudo -u postgres psql -v ON_ERROR_STOP=1 <<SQL
DO
\$\$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '${DB_USER}') THEN
    EXECUTE format('CREATE ROLE %I LOGIN PASSWORD %L', '${DB_USER}', '${DB_PASS}');
  END IF;
END
\$\$;
SQL

  # Create database if not exists
  sudo -u postgres psql -v ON_ERROR_STOP=1 <<SQL
SELECT format('CREATE DATABASE %I OWNER %I', '${DB_NAME}', '${DB_USER}')
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '${DB_NAME}')
\gexec
SQL

  # Grant privileges
  sudo -u postgres psql -v ON_ERROR_STOP=1 <<SQL
SELECT format('GRANT ALL PRIVILEGES ON DATABASE %I TO %I', '${DB_NAME}', '${DB_USER}')
\gexec
SQL

  log "Database '${DB_NAME}' and user '${DB_USER}' are ready."
}

create_group_and_user() {
  info "Creating Linux group and system user..."
  getent group "$APP_GROUP" >/dev/null || groupadd --system "$APP_GROUP"
  if ! id "$APP_USER" >/dev/null 2>&1; then
    useradd --system --gid "$APP_GROUP" --shell /usr/sbin/nologin \
      --create-home --home-dir "/home/$APP_USER" "$APP_USER"
  fi
  log "Group '$APP_GROUP' and user '$APP_USER' created."
}

deploy_application() {
  info "Creating application directory at $APP_DIR..."
  mkdir -p "$APP_DIR"

  if [[ -n "$APP_ARCHIVE" ]]; then
    if [[ ! -f "$APP_ARCHIVE" ]]; then
      err "App archive not found: $APP_ARCHIVE"
      exit 1
    fi

    info "Deploying application archive: $APP_ARCHIVE"
    case "$APP_ARCHIVE" in
      *.zip|*.ZIP)
        info "Extracting ZIP archive (flattening top folder)..."
        tmpdir=$(mktemp -d)

        # capture exit code and allow warning
        # Try to unzip quietly into temporary directory
        if unzip -q "$APP_ARCHIVE" -d "$tmpdir"; then
          (
            # include hidden files (.*) and avoid literal * when empty
            shopt -s dotglob nullglob

            # entries = top-level items after unzip
            entries=( "$tmpdir"/* )

            if [[ ${#entries[@]} -eq 1 && -d "${entries[0]}" ]]; then
              # case: zip has one top folder → move its contents
              mv "${entries[0]}"/* "$APP_DIR"/
            else
              # case: zip already has files at root → move them directly
              mv "$tmpdir"/* "$APP_DIR"/
            fi
          )
        else
          status=$?
          # If warning it is allow it
          if [[ ${status} -eq 1 ]]; then
           log "ZIP extracted with warnings (exit=${status})."
          else
            err "unzip failed (exit=${status})."
            rm -rf "$tmpdir"
            exit ${status}
          fi
        fi

        # Clean up the temporary directory
        rm -rf "$tmpdir"
        ;;
      *.tar.gz|*.tgz) tar -xzf "$APP_ARCHIVE" -C "$APP_DIR" ;;
      *.tar)  tar -xf "$APP_ARCHIVE" -C "$APP_DIR" ;;
      *)      err "Unsupported archive type: $APP_ARCHIVE"; exit 1 ;;
    esac
  else
    info "No application archive provided. Directory created but empty."
  fi
}

set_permissions() {
  info "Setting file ownership and permissions..."
  chown -R "$APP_USER:$APP_GROUP" "$APP_DIR"

  # Directories: 750  |  Files: 640  | Executable scripts: 750
  find "$APP_DIR" -type d -exec chmod 750 {} +
  find "$APP_DIR" -type f -exec chmod 640 {} +

  # Apply 750 permissions to executable files (shell scripts, binaries, and Maven wrapper)
  find "$APP_DIR" -type f \( -name "*.sh" -o -name "*.run" -o -name "*.bin" -o -name "mvnw" \) -exec chmod 750 {} +
  log "Permissions applied successfully."
}

clean_up() {
  rm -rf /var/lib/apt/lists/*
}

run_application() {
  info "Starting Spring Boot application..."

  # Ensure the wrapper exists
  if [[ ! -f "$APP_DIR/mvnw" ]]; then
    err "mvnw not found in $APP_DIR. Cannot start application."
    return 1
  fi

  cd "$APP_DIR"
  chmod +x mvnw

  # Start the application in the background
  # Hide all normal output (stdout) and keep only errors (stderr)
  nohup ./mvnw spring-boot:run >/dev/null 2>>/var/log/cloud-native-web-error.log &

  log "Application started successfully (errors logged to /var/log/cloud-native-web-error.log)"
}

main() {
  require_root
  copy_app_env
  ensure_env_file
  update_system
  install_common_tools
  install_java
  install_database
  if [[ "$DB_TYPE" == "postgres" || "$DB_TYPE" == "postgresql" ]]; then
    create_postgres_db
  fi
  create_group_and_user
  deploy_application
  set_permissions
  clean_up

  echo
  log "Setup completed successfully!"
  echo "------------------------------------------"
  echo "Database Type : $DB_TYPE"
  echo "Database Name : $DB_NAME"
  echo "Database User : $DB_USER"
  echo "App Directory : $APP_DIR"
  echo "App User      : $APP_USER ($APP_GROUP)"
  echo "APP_ARCHIVE  : ${APP_ARCHIVE:-<none>}"
  echo "------------------------------------------"
  info "Verification tips:"
  echo "  sudo -u postgres psql -c '\\l'      # List databases"
  echo "  sudo -u postgres psql -c '\\du'     # List users"

  run_application
}

main "$@"
