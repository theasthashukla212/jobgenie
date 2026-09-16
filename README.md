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
| Module | Responsibility |
|---|---|
| **Authentication** | User registration, login, and JWT token issuance/validation via `AuthenticationController`, `JwtService`, and `JwtAuthenticationFilter` |
| **Jobs** | CRUD operations on job postings via `JobController` and the `Job` entity |
| **Resumes** | Upload/manage candidate resumes via `ResumeController` and the `Resume` entity |
| **Applications** | Track job applications submitted by users via `ApplicationController` and the `Application` entity |

## Getting Started

### Prerequisites
- Java 17+ (JDK)
- Maven (or use the bundled `mvnw` wrapper)
- A relational database (configured in `application.properties`)

### Setup
```bash
# Clone the repository
git clone https://github.com/theasthashukla212/jobgenie.git
cd jobgenie

# Configure your database and JWT secret in:
# src/main/resources/application.properties

# Run using the Maven wrapper
./mvnw spring-boot:run       # Linux/Mac
mvnw.cmd spring-boot:run     # Windows

# Or build a JAR and run it
./mvnw clean package
java -jar target/*.jar
```

The API will be available at `http://localhost:8080` by default (adjust based on your `application.properties`).

### Running Tests
```bash
./mvnw test
```

## API Overview
| Endpoint Group | Description |
|---|---|
| `/api/auth/**` | Register, login, and JWT-based authentication |
| `/api/jobs/**` | Create, list, update, and delete job postings |
| `/api/resumes/**` | Upload and manage user resumes |
| `/api/applications/**` | Submit and track job applications |

> Exact route paths, request/response schemas, and status codes are defined in the respective controller classes under `controller/`.

## Related Repository
- **Frontend:** [jobgenie-ui](https://github.com/theasthashukla212/jobgenie-ui) — React + Vite single-page application that consumes this API.

## Roadmap
- Expand API documentation (OpenAPI/Swagger)
- Add role-based access control (candidate vs. recruiter vs. admin)
- Integrate AI-based resume tailoring and job-matching services with the backend
- Add pagination, filtering, and search for job listings

## Contributing
1. Fork the repository and create a feature branch.
2. Make your changes with clear, descriptive commits.
3. Open a pull request describing the change and its motivation.

## License
No license file is currently specified in this repository. Add one (e.g., MIT) if you intend to open source this project.
