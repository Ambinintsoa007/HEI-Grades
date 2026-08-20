# HEI Grades

> **Project status:** ✅ Completed - feature development is finished. The repository is now maintained only for evaluation, bug fixes, and operational adjustments.

HEI Grades is a serverless academic-grade management application built with **Spring Boot** and **POJA**. It manages students, teachers, academic courses, grades, transcripts, promotion results, graduate exports, and asynchronous transcript delivery while enforcing role-based access and grade traceability.

## Authors

- **RAZAFIMAMONJY Rocques NyAina Giovanni Nelio** -- K2 -- STD24114
- **RANDRIANARIVELO Ambinintsoa Dylan Nomenjanahary** -- K2 -- STD24214

## Technology Stack

| Area | Technologies                                                        |
| --- |---------------------------------------------------------------------|
| Language & runtime | Java 21                                                             |
| Backend | Spring Boot 3.2.2, Gradle                                           |
| Persistence | PostgreSQL, Spring Data JPA, Hibernate, Flyway                      |
| Security | Spring Security, stateless JWT authentication, OAuth2 Resource Server |
| Serverless platform | POJA, AWS Lambda                                                    |
| Asynchronous processing | AWS EventBridge, AWS SQS                                            |
| Storage & email | AWS S3, AWS SES                                                     |
| Document generation | Apache PDFBox, Apache POI (XLSX)                                    |
| Web UI | Thymeleaf                                                           |
| API contract | OpenAPI                                                             |
| Testing | JUnit 5, Spring Boot Test, Spring Security Test, Testcontainers     |
| Quality | JaCoCo, Google Java Format                                          |

## Domain Rules

The application implements the following core academic rules:

- A course may be taught by multiple teachers.
- A course may be offered by one or more teachers to several groups, without necessarily being offered to every group.
- A student may change groups during their academic path while preserving their academic history.
- A student can access only their own grades and transcripts.
- A teacher can view and modify grades only for courses they teach.
- An administrator has full access, including promotion results across the complete three-year curriculum.
- Every grade correction requires a reason and is recorded in an append-only history.
- A student is considered a graduate only when all courses actually followed during the required curriculum are complete and validated; there is no compensation between failed and passed courses.

## Architecture

The project follows a layered architecture with a strict separation between HTTP concerns, business logic, domain models, persistence, and infrastructure integrations.

```text
src/main/java/com/example/demo/
├── conf/                         # Spring, JWT and security configuration
├── endpoint/
│   ├── rest/
│   │   ├── controller/           # HTTP endpoints only
│   │   ├── dto/                  # Request/response contracts
│   │   └── exception/            # REST/business error handling
│   └── event/
│       ├── consumer/             # Asynchronous event consumption
│       └── model/                # Event payloads
├── service/
│   └── event/                    # Business services and async workers
├── model/                        # Domain models
├── repository/                   # Database access interfaces
│   └── model/                    # JPA persistence entities
├── mapper/                       # Domain ↔ persistence mapping
├── mail/                         # Email infrastructure
├── file/
│   └── bucket/                   # S3 storage infrastructure
├── handler/                      # AWS Lambda handlers
├── concurrency/                  # Runtime concurrency utilities
└── datastructure/                # Shared technical data structures
```

### Layer Responsibilities

| Layer | Responsibility |
| --- | --- |
| `endpoint/rest` | HTTP routing, validation and API DTOs |
| `service` | Business rules, authorization checks and orchestration |
| `model` | Business/domain representation without JPA concerns |
| `repository` | Database access |
| `repository/model` | JPA persistence entities |
| `mapper` | Conversion between domain models and persistence entities |
| `endpoint/event` | Event publication and consumption |
| `service/event` | Asynchronous business processing |
| `file` / `mail` | External storage and email integrations |

### Asynchronous Transcript Delivery

```text
Client
  │
  │ POST /students/{studentId}/transcript/email
  ▼
Frontal Lambda
  │
  ▼
EventBridge
  │
  ▼
SQS
  │
  ▼
Worker Lambda
  │
  ├── Generate PDF with PDFBox
  ├── Upload PDF to S3
  ├── Generate a temporary presigned S3 URL
  └── Send the URL by email through SES
```

The API returns `202 Accepted` when the request is successfully queued. The email contains a temporary presigned link to the generated transcript PDF.

## Getting Started

### Prerequisites

- Java 21
- PostgreSQL
- Bash-compatible shell or equivalent
- Docker, when running integration tests with Testcontainers

### Environment Configuration

Copy the environment template and provide the required values:

```bash
cp .env.template .env
```

The main configuration variables are:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
JWT_SECRET
AWS_EVENTBRIDGE_BUS
AWS_S3_BUCKET
```

Do not commit `.env`, database credentials, JWT secrets, AWS credentials, or test account passwords.

To load a local `.env` file in Bash and start the application:

```bash
set -a
source .env
set +a
./gradlew bootRun
```

## Authentication and Evaluation Accounts

Authentication is performed with:

```http
POST /auth/login
```

Example request:

```bash
curl -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "your-password"
  }'
```

The response contains a JWT and the authenticated user's role.

### Creating a STUDENT or TEACHER

User creation is intentionally protected and requires an authenticated **ADMIN** account.

For evaluation, use the ADMIN account provided separately by the project authors, authenticate through `/auth/login`, and use the returned JWT as `ADMIN_TOKEN`. Credentials are never committed to the repository.

To obtain a valid promotion ID for a student:

```bash
curl "$BASE_URL/promotions" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

Create a student:

```bash
curl -X POST "$BASE_URL/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "Student",
    "email": "student@example.com",
    "password": "change-me",
    "role": "STUDENT",
    "std": "STD-TEST-001",
    "promotionId": "<existing-promotion-uuid>"
  }'
```

Create a teacher:

```bash
curl -X POST "$BASE_URL/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "Teacher",
    "email": "teacher@example.com",
    "password": "change-me",
    "role": "TEACHER"
  }'
```

After creation, the new user can authenticate through `/auth/login` with their own credentials.

### ADMIN Access for Evaluation

There is **no public ADMIN registration flow**. Administrative account provisioning is deliberately restricted.

If an evaluator needs ADMIN access, does not have the evaluation ADMIN credentials, or wants to create an additional ADMIN user in the hosted evaluation environment, please **contact the project authors** **[Dylan](https://www.facebook.com/ripsouXD)** or **[Nelio](https://www.facebook.com/photo/?fbid=424743983845660&set=a.119083147745080)**. An existing administrator is required to authorize administrative user creation.

No administrative credentials are stored in this README or committed to the repository.

## Testing and Coverage

Run the complete test suite with:

```bash
./gradlew clean test
```

The project includes unit and integration tests using Spring Boot Test and Testcontainers.

The latest verified JaCoCo report in this project snapshot reports **95.45% line coverage**, above the required 80% threshold.

The HTML coverage report is generated at:

```text
build/reports/jacoco/test/html/index.html
```

## API Documentation

The complete API contract is available in:

```text
doc/api.yml
```
This file should be used as the primary technical reference for endpoint contracts.

## Code Formatting

Before committing changes, run:

```bash
./format.sh
```

Then verify the project with:

```bash
./gradlew clean test
```

## License / Academic Context

This repository was developed as an academic project for HEI. It is intended for evaluation and educational purposes.


### Contact :
*[hei.ambinintsoa@gmail.com](hei.ambinintsoa@gmail.com)* and *[hei.rocques@gmail.com](hei.rocques@gmail.com)* 