# Mini Quora

A compact Spring Boot question-and-answer application. It includes a
thin controller/service/DTO example, request validation, consistent problem responses,
Actuator health checks, tests, formatting checks, and CI.

## Prerequisites

- JDK 21 or newer
- Maven 3.9 or newer

## Run the application

```bash
mvn spring-boot:run
```

The application starts on `http://localhost:8080`.

## API

### Get a greeting

```bash
curl http://localhost:8080/api/greetings/Ishan
```

```json
{"message":"Hello, Ishan!"}
```

Names are required and limited to 50 characters. Invalid requests return an
`application/problem+json` response.

### Questions

Create a question:

```bash
curl -i -X POST http://localhost:8080/questions \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "How does optimistic locking work?",
    "body": "I want to prevent lost updates.",
    "authorId": "user-123",
    "tags": ["java", "concurrency"]
  }'
```

The response is `201 Created` with the question in the body and its URL in the
`Location` header. Question IDs are generated UUIDs. `authorId` and `tags` are
optional.

```bash
curl http://localhost:8080/questions/{questionId}
curl http://localhost:8080/questions
curl 'http://localhost:8080/questions?query=locking'
```

Search is a case-insensitive match across title, body, and tags. Upvote a
question with:

```bash
curl -X PUT http://localhost:8080/questions/{questionId}/votes/{userId}
```

An upvote is idempotent for each user and question: repeating the same request
does not increase `voteCount`. All question and vote data is held in memory and
is lost when the application restarts.

### Check application health

```bash
curl http://localhost:8080/actuator/health
```

Only the safe `health` and `info` Actuator endpoints are exposed.

## Build and test

```bash
mvn verify
```

To apply the configured Java formatter:

```bash
mvn spotless:apply
```

## Editor setup

When Cursor opens the project, install the recommended extensions when prompted. Java
files are then formatted with Palantir Java Format and imports are organized whenever
you save.

The editor and Maven use the same pinned formatter version. `mvn verify` also runs
Spotless and strict Java compiler linting, so CI rejects unformatted code, wildcard
imports, and compiler warnings.

## Project structure

```text
src/main/java/com/ishan/miniquora/
├── controller/   HTTP request and response handling
├── dto/          API request and response types
├── exception/    Consistent Problem Detail error handling
├── model/        Internal domain types
├── repository/   In-memory data storage
└── service/      Business logic
```

When extending Mini Quora:

1. Model the API contract in `dto`.
2. Keep business rules in `service`.
3. Keep controllers focused on HTTP concerns.
4. Add focused tests for happy paths and edge cases.
5. Add persistence, authentication, or external integrations as the application grows.

## Technology

- Java 21
- Spring Boot 4
- Maven
- JUnit 5 and MockMvc
- Spring Boot Actuator
