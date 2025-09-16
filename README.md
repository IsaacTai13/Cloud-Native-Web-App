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

## Env variable

Create your .env file in the project root directory (the same level as pom.xml).

```properties
DB_URL=
DB_USERNAME=
DB_PASSWORD=
SERVER_PORT=
```

## Run the Application

2. Build & start the app: `./mvnw spring-boot:run`

## Health Check Endpoints

1. /healthz
   - Method GET
   - Inserts a record into the `health_checks` table on every request
   - Return only HTTP status codes (no response body)
   - Cache is disabled
2. /api/health
   - Method GET
   - Detailed health probe
   - Does not insert records into the database