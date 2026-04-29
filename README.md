# Melikyan Academy

Melikyan Academy is a Spring Boot 4 backend for an online learning platform. It models a paid academic catalog, user access management, course and exam delivery, multilingual content, attendance tracking, student submissions, progress monitoring, and certificate issuance behind a session-based secured REST API.

This repository is not a toy CRUD sample. The codebase already contains:

- `244` main Java source files
- `50` test classes
- `25` controllers
- `24` services
- `41` entity classes
- Flyway-based schema management
- Swagger / OpenAPI documentation
- PostgreSQL persistence
- Redis-backed HTTP sessions
- Local filesystem storage for avatars and certificate PDFs

## What The System Does

At a product level, the application supports the full lifecycle of a training platform:

1. Users register, log in, obtain a session, and optionally use remember-me.
2. Admins and professors create academic content and package it into sellable products.
3. Users receive access to purchased or manually granted products through product registrations.
4. Access grants bootstrap per-user progress records for each linked content item.
5. Courses contain lessons and homework; exams contain sections and tasks.
6. Students submit homework and exam answers.
7. Attendance is tracked per lesson, including self check-in and automated enrollment/missed generation.
8. Completed content can be certified with uploaded PDF certificates.
9. Nearly every user-facing learning object can be localized through dedicated translation APIs.

## Core Domain Model

The domain is structured around a few central concepts.

### 1. Users and roles

The platform has three roles:

- `ADMIN`
- `PROFESSOR`
- `STUDENT`

Students are the default role on registration. Admins can seed an initial administrator account through configuration.

### 2. Content items

`ContentItem` is the root academic unit. It stores shared metadata such as:

- `type` (`COURSE` or `EXAM`)
- `title`
- `description`
- `totalSteps`
- `createdBy`

This is important because several workflows are built around content items rather than directly around courses or exams:

- product access is granted to content items
- progress is tracked per user and per content item
- certificates are issued per user and per content item
- translations for courses and exams share the same underlying content item translation model

### 3. Courses

A `Course` is a one-to-one specialization of a `ContentItem`. It adds:

- `startDate`
- `durationWeeks`
- assigned professors
- ordered lessons

Each course may have multiple professors and lessons.

### 4. Exams

An `Exam` is also a one-to-one specialization of a `ContentItem`. It is composed of:

- exam sections
- exam tasks
- student exam submissions

### 5. Products and access control

Products are the commercial packaging layer. A `Product` belongs to a `Category` and contains one or more content items through `ProductContentItem`.

Supported product types:

- `SINGLE`: must contain exactly one content item
- `PACKAGE`: may contain multiple content items

Notable behavior from the service layer:

- single-product titles can be derived from the primary content item when omitted
- single-product descriptions can fall back to the content item description
- package products require an explicit non-blank title
- product titles are enforced as unique

### 6. Registrations and progress

`ProductRegistration` is the access grant record between a user and a product. Granting access can optionally be tied to a successful `Transaction`.

When access is granted, the service creates initial `UserProcess` records for each linked content item if they do not already exist. That makes product activation the bridge between catalog ownership and learning progress.

### 7. Learning delivery

For course delivery, the main chain is:

`Course -> Lesson -> Homework -> HomeworkTask -> HomeworkSubmission`

Lessons include:

- order index
- session type (`MEET_LINK`, `VIDEO_LINK`)
- delivery URL
- lesson state (`SCHEDULED`, `ONGOING`, `COMPLETED`, `CANCELED`)
- start time
- duration

Homeworks are ordered inside lessons and can be published or unpublished.

### 8. Attendance

Lesson attendance supports three states:

- `ENROLLED`
- `ATTENDED`
- `MISSED`

Operational workflows already implemented:

- manual attendance creation
- student self check-in by lesson
- auto-generate enrolled records for active registrants
- auto-generate missed records after lesson completion

### 9. Certificates

Certificates are issued per user and content item, with:

- a generated `CERT-...` code
- issue date
- optional expiry date
- JSON metadata
- uploaded PDF file path
- issuer reference

Issuing a certificate is guarded by business rules:

- the user must have active access to the content item
- the user must have completed the content item based on `UserProcess.currentStep`
- only one certificate may exist per user/content item pair

## API Surface

All application endpoints live under `/api/v1` except Swagger/OpenAPI routes.

The authentication model is cookie/session based, not token/JWT based.

### Complete endpoint inventory

#### AuthController

- `GET /api/v1/auth/csrf`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/logout`

#### UserController

- `GET /api/v1/users/me`
- `PATCH /api/v1/users/me` `multipart/form-data`
- `PATCH /api/v1/users/me/password`
- `DELETE /api/v1/users/me`

#### UserProcessController

- `GET /api/v1/me/progress`
- `GET /api/v1/me/progress/content-items/{contentItemId}`
- `GET /api/v1/users/{userId}/progress`
- `GET /api/v1/users/{userId}/progress/content-items/{contentItemId}`

#### CategoryController

- `POST /api/v1/categories`
- `GET /api/v1/categories/{id}`
- `GET /api/v1/categories`
- `PATCH /api/v1/categories/{id}`
- `DELETE /api/v1/categories/{id}`

#### LanguageController

- `POST /api/v1/languages`
- `GET /api/v1/languages/{id}`
- `GET /api/v1/languages/code/{code}`
- `GET /api/v1/languages`
- `PATCH /api/v1/languages/{id}`
- `DELETE /api/v1/languages/{id}`

#### ProductController

- `POST /api/v1/products`
- `GET /api/v1/products/{id}`
- `GET /api/v1/products`
- `PATCH /api/v1/products/{id}`
- `DELETE /api/v1/products/{id}`

#### ProductRegistrationController

- `POST /api/v1/product-registrations/grant`
- `GET /api/v1/product-registrations/{id}`
- `GET /api/v1/product-registrations/users/{userId}`
- `GET /api/v1/product-registrations/products/{productId}`
- `GET /api/v1/product-registrations/me`
- `GET /api/v1/product-registrations/me/{id}`
- `PATCH /api/v1/product-registrations/{id}/activate`
- `PATCH /api/v1/product-registrations/{id}/suspend`
- `PATCH /api/v1/product-registrations/{id}/expire`

#### CourseController

- `POST /api/v1/courses`
- `GET /api/v1/courses/{id}`
- `GET /api/v1/courses`
- `PATCH /api/v1/courses/{id}`
- `DELETE /api/v1/courses/{id}`

#### LessonController

- `POST /api/v1/lessons`
- `GET /api/v1/lessons/{id}`
- `GET /api/v1/lessons`
- `GET /api/v1/lessons?courseId={courseId}`
- `PATCH /api/v1/lessons/{id}`
- `DELETE /api/v1/lessons/{id}`

#### LessonAttendanceController

- `POST /api/v1/lesson-attendances`
- `POST /api/v1/lesson-attendances/lessons/{lessonId}/check-in`
- `POST /api/v1/lesson-attendances/lessons/{lessonId}/generate-enrolled`
- `POST /api/v1/lesson-attendances/lessons/{lessonId}/generate-missed`
- `GET /api/v1/lesson-attendances/me`
- `GET /api/v1/lesson-attendances/me/{id}`
- `GET /api/v1/lesson-attendances/me/lessons/{lessonId}`
- `GET /api/v1/lesson-attendances/{id}`
- `GET /api/v1/lesson-attendances/lessons/{lessonId}`
- `PATCH /api/v1/lesson-attendances/{id}`
- `DELETE /api/v1/lesson-attendances/{id}`

#### HomeworkController

- `POST /api/v1/homeworks`
- `GET /api/v1/homeworks/{id}`
- `GET /api/v1/homeworks`
- `GET /api/v1/homeworks?lessonId={lessonId}`
- `PATCH /api/v1/homeworks/{id}`
- `DELETE /api/v1/homeworks/{id}`

#### HomeworkTaskController

- `POST /api/v1/homework-tasks`
- `GET /api/v1/homework-tasks/{id}`
- `GET /api/v1/homework-tasks`
- `GET /api/v1/homework-tasks?homeworkId={homeworkId}`
- `PATCH /api/v1/homework-tasks/{id}`
- `DELETE /api/v1/homework-tasks/{id}`

#### HomeworkSubmissionController

- `POST /api/v1/homework-submissions`
- `GET /api/v1/homework-submissions/me`
- `GET /api/v1/homework-submissions/me/{id}`
- `GET /api/v1/homework-submissions/me/task/{taskId}`
- `GET /api/v1/homework-submissions/{id}`
- `GET /api/v1/homework-submissions/task/{taskId}`
- `PATCH /api/v1/homework-submissions/{id}`
- `DELETE /api/v1/homework-submissions/me/{id}`
- `DELETE /api/v1/homework-submissions/{id}`

#### ExamController

- `POST /api/v1/exams`
- `GET /api/v1/exams/{id}`
- `GET /api/v1/exams`
- `PATCH /api/v1/exams/{id}`
- `DELETE /api/v1/exams/{id}`

#### ExamSectionController

- `POST /api/v1/exam-sections`
- `GET /api/v1/exam-sections/{id}`
- `GET /api/v1/exam-sections`
- `GET /api/v1/exam-sections?examId={examId}`
- `PATCH /api/v1/exam-sections/{id}`
- `DELETE /api/v1/exam-sections/{id}`

#### ExamTaskController

- `POST /api/v1/exam-tasks`
- `GET /api/v1/exam-tasks/{id}`
- `GET /api/v1/exam-tasks`
- `GET /api/v1/exam-tasks?sectionId={sectionId}`
- `PATCH /api/v1/exam-tasks/{id}`
- `DELETE /api/v1/exam-tasks/{id}`

#### ExamSubmissionController

- `POST /api/v1/exam-submissions`
- `GET /api/v1/exam-submissions/me`
- `GET /api/v1/exam-submissions/me/{id}`
- `GET /api/v1/exam-submissions/me/task/{taskId}`
- `GET /api/v1/exam-submissions/{id}`
- `GET /api/v1/exam-submissions/task/{taskId}`
- `PATCH /api/v1/exam-submissions/{id}`
- `DELETE /api/v1/exam-submissions/me/{id}`
- `DELETE /api/v1/exam-submissions/{id}`

#### CertificateController

- `POST /api/v1/certificates/issue` `multipart/form-data`
- `GET /api/v1/certificates/me`
- `GET /api/v1/certificates/verify/{certificateCode}`
- `GET /api/v1/certificates/{id}`
- `GET /api/v1/certificates/users/{userId}`
- `GET /api/v1/certificates/content-items/{contentItemId}`
- `PATCH /api/v1/certificates/{id}` `multipart/form-data`
- `DELETE /api/v1/certificates/{id}`

#### ProfessorController

- `POST /api/v1/professors`
- `POST /api/v1/professors/assign`
- `GET /api/v1/professors/{id}`
- `GET /api/v1/professors/courses/{courseId}`
- `GET /api/v1/professors/users/{userId}`
- `GET /api/v1/professors`
- `DELETE /api/v1/professors/users/{userId}/courses/{courseId}`

#### CourseTranslationController

- `POST /api/v1/course-translations`
- `GET /api/v1/course-translations/{id}`
- `GET /api/v1/course-translations`
- `GET /api/v1/course-translations/code/{code}`
- `GET /api/v1/course-translations/course/{courseId}`
- `GET /api/v1/course-translations/course/{courseId}/code/{code}`
- `PATCH /api/v1/course-translations/{id}`
- `DELETE /api/v1/course-translations/{id}`

#### ExamTranslationController

- `POST /api/v1/exam-translations`
- `GET /api/v1/exam-translations/{id}`
- `GET /api/v1/exam-translations`
- `GET /api/v1/exam-translations/code/{code}`
- `GET /api/v1/exam-translations/exam/{examId}`
- `GET /api/v1/exam-translations/exam/{examId}/code/{code}`
- `PATCH /api/v1/exam-translations/{id}`
- `DELETE /api/v1/exam-translations/{id}`

#### LessonTranslationController

- `POST /api/v1/lesson-translations`
- `GET /api/v1/lesson-translations/{id}`
- `GET /api/v1/lesson-translations`
- `GET /api/v1/lesson-translations/code/{code}`
- `GET /api/v1/lesson-translations/lesson/{lessonId}`
- `GET /api/v1/lesson-translations/lesson/{lessonId}/code/{code}`
- `PATCH /api/v1/lesson-translations/{id}`
- `DELETE /api/v1/lesson-translations/{id}`

#### HomeworkTranslationController

- `POST /api/v1/homework-translations`
- `GET /api/v1/homework-translations/{id}`
- `GET /api/v1/homework-translations`
- `GET /api/v1/homework-translations/code/{code}`
- `GET /api/v1/homework-translations/homework/{homeworkId}`
- `GET /api/v1/homework-translations/homework/{homeworkId}/code/{code}`
- `PATCH /api/v1/homework-translations/{id}`
- `DELETE /api/v1/homework-translations/{id}`

#### SectionTranslationController

- `POST /api/v1/section-translations`
- `GET /api/v1/section-translations/{id}`
- `GET /api/v1/section-translations`
- `GET /api/v1/section-translations/code/{code}`
- `GET /api/v1/section-translations/exam-section/{examSectionId}`
- `GET /api/v1/section-translations/exam-section/{examSectionId}/code/{code}`
- `PATCH /api/v1/section-translations/{id}`
- `DELETE /api/v1/section-translations/{id}`

#### ProductTranslationController

- `POST /api/v1/product-translations`
- `GET /api/v1/product-translations/{id}`
- `GET /api/v1/product-translations`
- `GET /api/v1/product-translations/code/{code}`
- `GET /api/v1/product-translations/product/{productId}`
- `GET /api/v1/product-translations/product/{productId}/code/{code}`
- `PATCH /api/v1/product-translations/{id}`
- `DELETE /api/v1/product-translations/{id}`

Swagger UI is available at:

- `http://localhost:8080/swagger-ui.html`

OpenAPI JSON is available at:

- `http://localhost:8080/v3/api-docs`

The OpenAPI config also enriches endpoint descriptions with role access information derived from `@PreAuthorize`.

## Security Model

Security is one of the stronger parts of the project.

### Authentication strategy

The application uses:

- Spring Security with session-based authentication
- `HttpSessionSecurityContextRepository`
- Redis-backed sessions via Spring Session
- custom remember-me token storage in the database
- CSRF protection with cookie token repository

### Password handling

Passwords are encoded with Argon2:

- `Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()`

### Remember-me implementation

Remember-me is not using the default Spring Security JDBC remember-me implementation. Instead, the project implements:

- selector/token cookie format
- HMAC-based token hashing
- persistent remember-me token table
- token rotation on successful remember-me authentication
- per-user token invalidation on logout and password change

That is a solid design choice and notably better than storing raw long-lived tokens.

### Authorization rules

Global rules in `SecurityConfig`:

- Swagger and auth bootstrap routes are public
- everything else requires authentication
- method-level role checks are enforced with `@PreAuthorize`

Typical patterns:

- `ADMIN` only for category and language management
- `ADMIN` or `PROFESSOR` for teaching content and certification operations
- authenticated user access for self-service data like progress, registrations, attendance, and certificates

### Headers and browser protections

The app configures:

- content security policy
- frame denial
- content type sniffing protection
- no-referrer policy

## Persistence And Data Strategy

### Database

The persistence layer uses:

- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway for schema versioning

DDL is set to `validate`, which means the application expects the database schema to already match the entity model and relies on Flyway to manage structure.

### Schema design notes

The initial migration defines:

- PostgreSQL enum types for roles, statuses, lesson modes, task types, payment methods, and more
- explicit constraints for blank checks and positive numeric validation
- many unique constraints for domain invariants
- soft-delete-compatible tables with `deleted_at`

### Soft deletes

Many entities use Hibernate soft delete with timestamp strategy:

- users
- categories
- products
- content items
- lessons
- homeworks
- certificates
- and others

This means deletions are generally logical, not physical, which is appropriate for academic and audit-sensitive data.

## Localization Design

Localization is handled explicitly rather than implicitly.

The system contains:

- a `Language` entity and API
- shared content item translations for both courses and exams
- dedicated translation tables and APIs for lessons, homework, exam sections, and products

Translation codes are normalized to two-character lowercase language codes, and translation services validate that the referenced language exists.

This is a clean design because it separates canonical academic data from language-specific presentation data.

## File Storage

The application currently stores uploaded files on the local filesystem:

- avatars under `uploads/avatars`
- certificate PDFs under `uploads/certificates`

Implemented protections include:

- avatar content-type validation: `image/jpeg`, `image/png`, `image/webp`
- avatar max size: `5 MB`
- certificate PDF extension validation
- certificate file signature validation for `%PDF-`

Operational note:

- the storage services return paths like `/uploads/avatars/...` and `/uploads/certificates/...`
- there is no explicit resource handler in this repository for serving those files directly

If these files are meant to be browser-accessible, either the application or a reverse proxy will need to expose that path.

## Configuration

The app imports environment variables from `.env` through:

```yaml
spring.config.import: optional:file:.env[.properties]
```

### Required environment variables

| Variable | Purpose |
| --- | --- |
| `DB_URL` | JDBC URL for PostgreSQL |
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `REMEMBER_ME_HMAC_SECRET` | HMAC secret for remember-me token hashing |

### Variables used by Docker Compose

| Variable | Purpose |
| --- | --- |
| `DB_NAME` | Postgres database name |
| `DB_PORT` | Host port mapped to PostgreSQL |
| `REDIS_PORT` | Host port mapped to Redis |

### Optional admin seeding variables

| Variable | Purpose |
| --- | --- |
| `APP_ADMIN_SEED_ENABLED` | Enables automatic admin creation |
| `APP_ADMIN_EMAIL` | Seeded admin email |
| `APP_ADMIN_PASSWORD` | Seeded admin password |
| `APP_ADMIN_FIRST_NAME` | Seeded admin first name |
| `APP_ADMIN_LAST_NAME` | Seeded admin last name |

### Example `.env`

```dotenv
DB_NAME=melikyan_academy
DB_PORT=5432
DB_URL=jdbc:postgresql://localhost:5432/melikyan_academy
DB_USERNAME=postgres
DB_PASSWORD=postgres

REDIS_PORT=6379

REMEMBER_ME_HMAC_SECRET=replace-with-a-long-random-secret

APP_ADMIN_SEED_ENABLED=true
APP_ADMIN_EMAIL=admin@example.com
APP_ADMIN_PASSWORD=change-me
APP_ADMIN_FIRST_NAME=System
APP_ADMIN_LAST_NAME=Admin
```

## Running The Project Locally

### Prerequisites

- Java `21`
- Docker and Docker Compose

### 1. Start infrastructure

```bash
docker compose up -d
```

This brings up:

- PostgreSQL `18`
- Redis `8`

### 2. Provide environment variables

Create a `.env` file in the project root using the example above.

### 3. Start the application

```bash
./mvnw spring-boot:run
```

### 4. Run tests

```bash
./mvnw test
```

## Package Layout

The package structure is conventional and easy to navigate:

```text
src/main/java/com/melikyan/academy
├── config        # security, OpenAPI, CORS, admin bootstrap
├── controller    # REST layer
├── dto           # request/response contracts
├── entity        # JPA domain model
├── exception     # API error handling
├── mapper        # MapStruct mapping layer
├── repository    # Spring Data repositories
├── security      # remember-me and auth infrastructure
├── service       # business logic
└── storage       # local file persistence
```

This is a good separation of concerns for a Spring MVC service. The controllers are thin, the business rules mostly live in services, and mapping is delegated to MapStruct instead of hand-written DTO assembly.

## Validation, Errors, And API Behavior

The project includes a centralized `GlobalExceptionHandler` that returns structured error responses for:

- validation failures
- bad requests
- conflicts
- unauthorized access
- forbidden access
- not found routes
- unsupported methods
- generic server errors

This is a strong baseline for frontend integration because error output is normalized across the API.

## API Contract Conventions

The API is broad, but the contract style is fairly consistent.

### Transport and format

- REST endpoints are JSON by default
- create endpoints usually return `201 Created`
- successful updates often return `200 OK` with the updated resource
- delete and some mutation endpoints return `204 No Content`
- multipart is used only where files are involved

### Identifier and type conventions

- primary identifiers are UUIDs
- most entity endpoints use UUID path variables
- certificate verification uses a human-readable certificate code instead of UUID
- enums are persisted and exposed as uppercase string values

### Timestamp conventions

- the application sets Jackson timezone to `UTC`
- entities commonly use `OffsetDateTime`
- audit fields such as `createdAt` and `updatedAt` are generated server-side

### Authentication contract

Frontend clients should assume this flow:

1. `GET /api/v1/auth/csrf`
2. store the CSRF token value from the response
3. send it back on mutating requests through the `X-XSRF-TOKEN` header
4. authenticate with `POST /api/v1/auth/login`
5. rely on cookies for session continuity

The API is not designed around bearer tokens. Clients are expected to operate in a cookie-aware environment.

### Multipart endpoints

The following areas use multipart payloads:

- profile update for avatar upload
- certificate issue for PDF upload
- certificate update for optional PDF replacement

For certificates specifically:

- the request body is split into `request` and `certificate` parts
- the `certificate` part must be a valid PDF

### Error contract

Error responses follow a structured shape with fields such as:

- `timestamp`
- `status`
- `error`
- `message`
- `path`
- `validationErrors` when applicable

Validation errors are aggregated into a field-name-to-message map, which is useful for frontend forms.

### Access contract

The practical access model for client developers is:

- public: Swagger docs, API docs, CSRF bootstrap, register, login, certificate verification
- authenticated user: self profile, self registrations, self progress, self attendance, self certificates, submissions
- admin/professor: academic content management, grading-related operations, registrations, certificates
- admin only: language management, category management, professor assignment

### Contract caveats

- not every endpoint is fully self-descriptive by naming alone; some workflow knowledge is assumed
- product access rules are enforced in services, not just at the controller layer
- some business transitions such as registration activation/suspension/expiry are modeled as dedicated endpoints rather than generic field updates

## Operational Assumptions

The codebase already encodes several assumptions that are worth making explicit.

### Infrastructure assumptions

- PostgreSQL is required for normal application startup
- Redis is expected for session storage behavior
- Flyway migrations are expected to be the source of truth for schema creation
- the default schema is `public`

### Deployment assumptions

- the service is intended to run behind HTTPS in real environments
- secure cookies are enabled for CSRF and remember-me behavior
- CORS is expected to be configured explicitly through `app.cors.allowed-origins`

### Data lifecycle assumptions

- many deletions are soft deletes
- domain records may remain historically relevant after removal from active use
- uniqueness and integrity rules are enforced both in code and in the database

### Learning workflow assumptions

- users must have active product registration to access content-driven actions
- progress is tracked per content item, not per product
- certificate issuance assumes completion is represented by `currentStep >= totalSteps`
- attendance automation assumes lesson state transitions are meaningful and maintained correctly

### Content modeling assumptions

- a course and an exam are both specialized content items
- translations are first-class records, not ad hoc localized fields on the base entity
- product packaging is separate from academic structure

### File handling assumptions

- uploaded files are stored on local disk
- the local `uploads` directory is treated as durable enough for development
- replacing an avatar or certificate may delete the previously stored file
- there is no object storage abstraction yet

### Testing assumptions

- many controller and service tests are isolated and mock-heavy
- the full application context test expects a reachable PostgreSQL database
- a contributor running the whole suite should expect environment-sensitive failures if the database is unavailable

### Maintainer assumptions

- API consumers are expected to read Swagger for endpoint-level detail
- business invariants are primarily enforced in services
- the schema migration file is a critical source of truth and should be reviewed alongside entity changes

## Test Coverage Status

The test suite is broad and intentionally mirrors the application layers:

- controller tests for endpoint behavior
- service tests for domain rules
- bootstrapping test for application context

At the time this README was generated, the repository contained `50` test classes, including coverage for:

- auth
- users
- categories
- products
- courses
- lessons
- homework
- exams
- submissions
- translations
- certificates
- lesson attendance
- progress tracking

## Notable Strengths

- Clear separation between commercial products and academic content
- Sensible use of `ContentItem` as a shared abstraction for courses and exams
- Session-based auth with explicit CSRF protection
- Custom remember-me flow with rotation and server-side persistence
- Consistent controller/service/repository layering
- Good use of database constraints to enforce invariants
- Soft-delete support across major entities
- Comprehensive localization surface
- Real test coverage instead of placeholder tests

## Important Operational Notes

These are the main things a new maintainer should know.

### 1. Redis is part of the auth/session architecture

This is not optional if you want production-like session behavior. The project depends on Spring Session Data Redis.

### 2. File uploads are local, not object storage based

That is fine for development and small deployments, but larger environments will likely want S3-compatible storage or another persistent shared volume strategy.

### 3. Cookie security may need local-environment attention

The CSRF and remember-me cookies are written as `Secure`. That is desirable for HTTPS deployments, but plain HTTP local development may require environment-specific adjustment if cookies are not being sent by the browser.

### 4. The transaction domain exists, but it is not yet a first-class public API

There are entities, DTOs, mappers, and repositories for transactions, and product registration can validate successful transactions, but there is no public transaction controller/service pair exposed in this repository. That suggests the billing workflow is either still in progress or meant to be integrated from another boundary.

### 5. `HELP.md` is just generated starter content

The meaningful project documentation should live in this `README.md`.

## Suggested First Reads For New Contributors

If you are onboarding into the codebase, start here:

1. `src/main/resources/application.yaml`
2. `src/main/resources/db/migration/V1__init.sql`
3. `src/main/java/com/melikyan/academy/config/SecurityConfig.java`
4. `src/main/java/com/melikyan/academy/service/AuthService.java`
5. `src/main/java/com/melikyan/academy/service/ProductService.java`
6. `src/main/java/com/melikyan/academy/service/ProductRegistrationService.java`
7. `src/main/java/com/melikyan/academy/service/CertificateService.java`
8. `src/main/java/com/melikyan/academy/service/LessonAttendanceService.java`

Those files explain most of the architecture and the most important business rules.

## Current Overall Assessment

This is a substantial backend foundation for an academy platform. The architecture is conventional in a good way, the domain model is richer than average, and the team has already implemented several real business workflows beyond basic CRUD. The main areas that stand out for future evolution are deployment-hardening for file storage, completion of the transaction/payment boundary, and continued documentation of API contracts and operational assumptions.
