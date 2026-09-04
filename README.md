# Machine Coding API

A small Spring Boot foundation for API-focused machine-coding exercises. It includes a
thin controller/service/DTO example, request validation, consistent problem responses,
Actuator health checks, tests, formatting checks, and CI.

## Prerequisites

- JDK 21 or newer
- No local Maven installation is required; the Maven Wrapper is included

## Run the application

```bash
./mvnw spring-boot:run
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

### Check application health

```bash
curl http://localhost:8080/actuator/health
```

Only the safe `health` and `info` Actuator endpoints are exposed.

## Build and test

```bash
./mvnw verify
```

To apply the configured Java formatter:

```bash
./mvnw spotless:apply
```

## Project structure

```text
src/main/java/com/ishan/machinecodingapi/
├── controller/   HTTP request and response handling
├── dto/          API request and response types
├── exception/    Consistent Problem Detail error handling
└── service/      Business logic
```

When extending the starter:

1. Model the API contract in `dto`.
2. Keep business rules in `service`.
3. Keep controllers focused on HTTP concerns.
4. Add focused tests for happy paths and edge cases.
5. Add persistence, authentication, or external integrations only when the exercise
   requires them.

## Technology

- Java 21
- Spring Boot 4
- Maven Wrapper
- JUnit 5 and MockMvc
- Spring Boot Actuator
