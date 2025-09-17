# Cloud-Native Web App

## Tech Stack

- Java (17+)
- Spring Boot
- Spring Data JPA (with Hibernate)
- Liquibase (for schema migrations)
- PostgreSQL 16

## Prerequisites

- Install PostgreSQL (v16+ recommended).
- IDE: IntelliJ IDEA (recommended).
- `.env` file in the project root directory

### Env variable

1. Run the following cmd to create a `.env` file in the project root directory (same level as `pom.xml`): 💡If you don’t have Git installed, just create the `.env` file manually in the project root.

    ```shell
    cd "$(git rev-parse --show-toplevel)" && cat > .env <<'EOF'
    # Database Connection
    DB_HOST=your_db_host
    DB_PORT=your_mapped_port
    DB_NAME=your_db_name
    DB_USERNAME=your_db_user
    DB_PASSWORD=your_db_password
    
    # Application
    SERVER_PORT=8081
    DB_CONN_TIMEOUT_MS=2000
    EOF
    ```

   - DB_CONN_TIMEOUT_MS controls the database connection timeout (in ms)
   - so health checks fail fast if the DB is unavailable.

2. Edit the .env file to match your local DB configuration.

### Install PostgreSQL using Docker

Docker Compose automatically loads environment variables from a .env file located in the project root directory. Make sure you create this file before starting the containers.

1. Create the docker-compose.yml file by running the following commands **in your project root directory** (same level as `pom.xml`).

    ```shell
    cat > docker-compose.yml <<'EOF'
    version: "3.9"
    services:
      postgres:
        image: postgres:16
        container_name: pg
        restart: always
        environment:
          POSTGRES_USER: ${DB_USERNAME}
          POSTGRES_PASSWORD: ${DB_PASSWORD}
          POSTGRES_DB: ${DB_NAME}
        ports:
          - "${DB_PORT}:5432"
        volumes:
          - pgdata:/var/lib/postgresql/data
    
    volumes:
      pgdata:
    EOF
    ```

2. Start PostgreSQL

    ```shell
    docker compose up -d
    ```

## Run the Application

Build & start the app:

```shell
./mvnw clean package
./mvnw spring-boot:run
```


## Health Check Endpoints

1. `/healthz`
   - Method: GET
   - Purpose: Lightweight liveness/readiness probe
   - Behavior:
     - Inserts a record into the health_checks table on every request
     - Cache disabled via Cache-Control: no-cache
     - Returns only HTTP status codes (no response body)
   - Status Codes:
     - 200 OK: DB insert successful
     - 400 Bad Request: Request contains a body (not allowed)
     - 405 Method Not Allowed: Non-GET method used
     - 503 Service Unavailable: DB unreachable or insert failed
   - Commands:
     - 200: `curl -vvvv http://localhost:8081/healthz`
     - 400: `curl -vvvv http://localhost:8081/healthz -X GET -H "Content-Type: application/json" -d '{"test":"value"}'`
     - 405: `curl -vvvv -XPUT http://localhost:8081/healthz`
     - 503:
       - stop the db: `docker compose stop postgres`
       - `curl -vvvv http://localhost:8081/healthz`
2. `/api/health`
   - Method: GET
   - Purpose: Detailed health probe
   - Behavior:
     - Does not insert into the database
     - Returns JSON with service and DB status, latency, and metadata
   - Status Codes:
     - 200 OK: Service healthy
     - 400 Bad Request: Request contains a body
     - 405 Method Not Allowed: Non-GET method used
     - 503 Service Unavailable: Dependency failure (with error details)