# Cloud-Native Web App

A Spring Boot–based cloud-native web application with PostgreSQL persistence, Liquibase schema migrations, and secured REST APIs for User and Product management.

---

## Prerequisites

- **Java**: JDK 17+
- **Maven**: Included via `./mvnw`
- **Docker & Docker Compose**: For containerized database setup
- **IDE**: IntelliJ IDEA (recommended)

---

## Framework & Dependencies

Defined in `pom.xml`:

- **Spring Boot 3.5.5**
    - `spring-boot-starter-web` (REST APIs)
    - `spring-boot-starter-data-jpa` (JPA/Hibernate ORM)
    - `spring-boot-starter-security` (authentication & authorization)
    - `spring-boot-starter-validation` (DTO validation)
- **Liquibase**: Database schema migrations
- **PostgreSQL**: Relational database
- **Lombok**: Boilerplate code reduction
- **Testing stack**:
  - **spring-boot-starter-test**: Testing utilities (JUnit, Mockito, etc.)
  - **junit-jupiter** (API, Engine, Params)
  - **rest-assured** & **json-schema-validator** – API integration tests
  - **hamcrest** – Matcher library for assertions

---

## 👾 Environment Configuration

### Env variable

The application loads environment variables from a `.env` file in the project root (same level as `pom.xml`).

Create `.env`:

1. Run the following cmd in the project root directory (same level as `pom.xml`)

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

### PostgreSQL with Docker (This can be used for local testing)

> ⭐The application now connects directly to an AWS RDS PostgreSQL instance instead of a local Docker container.

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
   
---

## 🚀 Build & Run

Build the project:

```shell
./mvnw clean package
```

Run the application:

```shell
./mvnw spring-boot:run
```

The app will start at: http://localhost:8081

---

## 📡 API Endpoints

### User APIs

- POST /v1/user – Create user (No Auth)
- PUT /v1/user/{id} – Update user (Requires Auth)
- GET /v1/user/{id} – Get user (Requires Auth)

### Product APIs

- GET /v1/product/{id} – List products (No Auth)
- POST /v1/product – Create product (Requires Auth)
- PUT /v1/product/{id} – Update product (Requires Auth)
- PATCH /v1/product/{id} – Partially update product (Requires Auth)
- DELETE /v1/product/{id} – Delete product (Requires Auth)

### Image APIs

- POST `/v1/product/{product_id}/image` – Upload an image for a product
- GET `/v1/product/{product_id}/image` – List all uploaded images for a product
- GET `/v1/product/{product_id}/image/{image_id}` – Retrieve specific image details
- DELETE `/v1/product/{product_id}/image/{image_id}` – Delete an image

---

## 💊 Health Check Endpoints

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

---

## 🧪 Testing

The project includes unit tests and integration tests:

- Integration tests: End-to-end API validation with REST Assured (positive, negative, and edge cases).
  - Use Spring Boot Test with a dedicated application-ci.yml.
  - Automatically runs Liquibase migrations against a real PostgreSQL instance.
  - Covers user and product API workflows, as well as health check endpoints.

Run Locally

```shell
./mvnw clean verify
```

Reports are generated under:

- target/surefire-reports/
- target/failsafe-reports/

---

## ⚙️ Continuous Integration (CI)

This repository uses GitHub Actions for CI:
- Workflow: .github/workflows/ci.yml
- Trigger: Runs on every pull request to main
- Pipeline includes:
  1. Checkout code and set up JDK 17
  2. Start a Postgres 16 service
  3. Run Liquibase migrations automatically on app startup
  4. Execute integration tests with Maven
  5. Upload test reports as GitHub artifacts for debugging

👉 CI ensures that all tests pass before a PR can be merged into main.

---

## 📦 Deployment

Ensure .env file is configured correctly. 
The app automatically connects to AWS RDS (ensure DB credentials in `.env` match your RDS configuration).

Build the JAR:

```shell
./mvnw clean package
```

Run with:

```shell
java -jar target/Cloud-Native-Web-0.0.1-SNAPSHOT.jar
```