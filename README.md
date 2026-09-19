# JobGenie — Backend

## Aim

JobGenie is an AI-assisted job search and career management platform. The backend is a **Spring Boot REST API** that powers user authentication, job listings, resume management, and job application tracking for the JobGenie web application. It is designed to give job seekers a single, secure place to manage their job search lifecycle — from storing resumes to tracking applications — while exposing clean REST endpoints that the [JobGenie UI](https://github.com/theasthashukla212/jobgenie-ui) (React frontend) consumes.

## Overview

This service follows a standard layered Spring Boot architecture (`controller` → `service` → `repository` → `model`) and secures its endpoints using **JWT-based authentication**. It acts as the single source of truth for:

- User accounts and authentication (registration, login, JWT issuance/validation)
- Job postings/listings
- Resumes uploaded/managed by users
- Job applications submitted by users and their status tracking

## Tech Stack

- **Language:** Java
- **Framework:** Spring Boot
- **Security:** Spring Security + JWT (JSON Web Tokens)
- **Build Tool:** Maven (`mvnw` / `mvnw.cmd` wrapper included)
- **Data Layer:** Spring Data JPA repositories

## Project Structure

```
jobgenie/
├── src/
│   ├── main/
│   │   ├── java/com/jobgenie/jobgenie_backend/
│   │   │   ├── JobgenieBackendApplication.java   # Spring Boot entry point
│   │   │   ├── config/                           # Security & JWT configuration
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtService.java
│   │   │   │   └── JwtAuthenticationFilter.java
│   │   │   ├── controller/                        # REST API endpoints
│   │   │   │   ├── AuthenticationController.java
│   │   │   │   ├── JobController.java
│   │   │   │   ├── ResumeController.java
│   │   │   │   └── ApplicationController.java
│   │   │   ├── dto/                                # Request/response payloads
│   │   │   ├── model/                              # JPA entities
│   │   │   │   ├── User.java
│   │   │   │   ├── Job.java
│   │   │   │   ├── Resume.java
│   │   │   │   └── Application.java
│   │   │   ├── repository/                         # Spring Data repositories
│   │   │   └── service/                            # Business logic
│   │   └── resources/
│   │       └── application.properties              # App & DB configuration
│   └── test/                                        # Unit/integration tests
├── pom.xml                                          # Maven dependencies
├── mvnw / mvnw.cmd                                  # Maven wrapper scripts
└── README.md
```

## Core Modules

| Module             | Responsibility                                                                                                                          |
| ------------------ | --------------------------------------------------------------------------------------------------------------------------------------- |
| **Authentication** | User registration, login, and JWT token issuance/validation via `AuthenticationController`, `JwtService`, and `JwtAuthenticationFilter` |
| **Jobs**           | CRUD operations on job postings via `JobController` and the `Job` entity                                                                |
| **Resumes**        | Upload/manage candidate resumes via `ResumeController` and the `Resume` entity                                                          |
| **Applications**   | Track job applications submitted by users via `ApplicationController` and the `Application` entity                                      |

## Getting Started

### Prerequisites

- JDK 25 (the current Maven compiler target; Spring Boot 3.5 is used)
- Maven (or use the bundled `mvnw` wrapper)
- H2 is used by default for development and tests. PostgreSQL is supported through environment variables.

### Setup

```bash
# Clone the repository
git clone https://github.com/theasthashukla212/jobgenie.git
cd jobgenie

# Configure environment variables when leaving the local H2 defaults.

# Run using the Maven wrapper
./mvnw spring-boot:run       # Linux/Mac
mvnw.cmd spring-boot:run     # Windows

# Or build a JAR and run it
./mvnw clean package
java -jar target/*.jar
```

The API will be available at `http://localhost:8080` by default (adjust based on your `application.properties`).

### Configuration

The following variables are supported:

| Variable                      | Default                        | Purpose                                                                |
| ----------------------------- | ------------------------------ | ---------------------------------------------------------------------- |
| `DB_URL`                      | `jdbc:h2:mem:testdb`           | JDBC URL; use `jdbc:postgresql://...` for PostgreSQL                   |
| `DB_DRIVER`                   | `org.h2.Driver`                | JDBC driver class                                                      |
| `DB_USERNAME` / `DB_PASSWORD` | `sa` / empty                   | Database credentials                                                   |
| `DDL_AUTO`                    | `update`                       | Hibernate schema strategy; use migrations and `validate` in production |
| `JWT_SECRET`                  | development-only Base64 secret | Base64 HMAC signing key; provide a private production value            |
| `JWT_EXPIRATION`              | `86400000`                     | Token lifetime in milliseconds                                         |
| `REFRESH_TOKEN_EXPIRATION`    | `2592000000`                   | Rotating refresh-token lifetime in milliseconds                        |
| `RESUME_STORAGE_DIR`          | `./uploads/resumes`            | Local multipart resume storage directory                               |
| `MAX_RESUME_FILE_SIZE`        | `5MB`                          | Maximum uploaded resume size                                           |
| `MAX_RESUME_REQUEST_SIZE`     | `6MB`                          | Maximum multipart request size                                         |
| `FRONTEND_ORIGINS`            | local Vite/React ports         | Comma-separated CORS origins                                           |
| `SERVER_PORT`                 | `8080`                         | HTTP port                                                              |
| `EXTERNAL_JOBS_URL`           | Arbeitnow public feed          | External job provider URL                                              |
| `AI_API_KEY`                  | empty                          | OpenAI-compatible API key for ATS and tailoring                        |
| `AI_MODEL`                    | `llama-3.3-70b-versatile`      | OpenAI-compatible chat model                                           |
| `AI_API_URL`                  | Groq chat completions URL      | OpenAI-compatible endpoint                                             |

For PostgreSQL, set `DB_URL`, `DB_DRIVER=org.postgresql.Driver`, `DB_USERNAME`, `DB_PASSWORD`, and preferably `DDL_AUTO=validate`. Never use the development JWT fallback in production.

### Running Tests

```bash
./mvnw test
```

### Provider Integrations

- `GET /api/jobs/external` fetches live jobs from Arbeitnow and accepts an optional `search` query.
- `POST /api/ai/ats-score` scores resume text against a job description. `POST /api/ai/tailor-resume` generates a tailored resume. Both use the configured OpenAI-compatible provider and fall back to local keyword analysis when `AI_API_KEY` is empty.

Example local configuration:

```bash
export AI_API_KEY=your-provider-key
./mvnw spring-boot:run
```

## API Overview

| Endpoint Group                                            | Description                                       |
| --------------------------------------------------------- | ------------------------------------------------- |
| `POST /api/auth/register`                                 | Register a USER and return a JWT                  |
| `POST /api/auth/login`                                    | Authenticate and return access and refresh tokens |
| `POST /api/auth/refresh`                                  | Rotate a refresh token and issue new credentials  |
| `GET /api/jobs?page=0&size=10&search=java&type=Full-time` | Public, stable paginated job search               |
| `GET /api/jobs/{id}`                                      | Public job detail                                 |
| `POST/PUT/DELETE /api/jobs[/{id}]`                        | RECRUITER or ADMIN job management                 |
| `GET/POST/PUT/DELETE /api/resumes[/{id}]`                 | Authenticated user-owned text/content resumes     |
| `POST /api/resumes/upload`                                | Authenticated PDF, DOC, DOCX, or TXT upload       |
| `GET /api/resumes/{id}/file`                              | Authenticated owner-only resume download          |
| `GET/POST/PUT/DELETE /api/applications[/{id}]`            | Authenticated user-owned applications             |
| `GET /api/admin/users`                                    | ADMIN-only safe user listing                      |
| `PATCH /api/admin/users/{id}/enabled?enabled=false`       | ADMIN-only account enable/disable                 |

> Exact route paths, request/response schemas, and status codes are defined in the respective controller classes under `controller/`.

### Example Requests

```json
POST /api/auth/register
{
	"email": "candidate@example.com",
	"password": "Password1",
	"firstName": "Ada",
	"lastName": "Lovelace"
}
```

```json
POST /api/applications
Authorization: Bearer <token>
{
	"jobId": 12,
	"resumeId": 4,
	"status": "APPLIED",
	"notes": "Submitted through the company portal"
}
```

Successful authentication returns `token`, `refreshToken`, `refreshTokenExpiresIn`, `id`, `email`, `firstName`, `lastName`, and `role`. Refresh tokens are opaque, stored only as hashes, and rotated on use. Dates are ISO-8601 values. Errors use `timestamp`, `status`, `error`, `message`, `path`, and `fieldErrors`.

### Frontend Integration

The React frontend should send `Authorization: Bearer <token>` for resumes, applications, and recruiter/admin job mutations. Use `http://localhost:8080` as the local API base URL and configure `FRONTEND_ORIGINS` to match the frontend origin. Job reads do not require a token; all user-owned resources derive the user from the token and ignore client-supplied user IDs.

### Known Limitations

- Local multipart resume storage is implemented. Production deployments should point `RESUME_STORAGE_DIR` at durable private storage or replace the storage service with object storage.
- There is no password reset or email verification flow yet because those require an email provider and delivery policy.
- The default H2 schema is suitable for development/tests. Production deployments should use PostgreSQL, a secret manager, and versioned migrations.
- OpenAPI is available at `/v3/api-docs` and Swagger UI at `/swagger-ui.html`.

## Related Repository

- **Frontend:** [jobgenie-ui](https://github.com/theasthashukla212/jobgenie-ui) — React + Vite single-page application that consumes this API.

## Roadmap

- Add password reset and email verification
- Add versioned PostgreSQL migrations
- Replace local resume storage with object storage

## Contributing

1. Fork the repository and create a feature branch.
2. Make your changes with clear, descriptive commits.
3. Open a pull request describing the change and its motivation.

## License

No license file is currently specified in this repository. Add one (e.g., MIT) if you intend to open source this project.
