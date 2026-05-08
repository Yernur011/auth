---
name: new-endpoint
description: Scaffold a complete new REST endpoint following the hexagonal architecture of this project. Creates all layers in order: domain command/model update → in-port interface → use-case method → REST DTO → controller method → AppConfig wiring. Use when adding any new API endpoint.
argument-hint: "<HTTP_METHOD> <path> — <description>  e.g. POST /auth/verify — verify email token"
---

Scaffold a new endpoint for this hexagonal auth microservice. Argument: `$ARGUMENTS`

## Current migration state
!`ls microservice/src/main/resources/db/migration/ 2>/dev/null`

## Steps — follow in order, write complete code for each file

### 1. Domain layer (`core`) — if new data is needed
- Add/update model in `kz.dev.core.model`
- Add command record if the endpoint takes input: `kz.dev.core.model`
- Add exception if new failure mode: `kz.dev.core.exception`
- **Zero Spring/JPA/Lombok annotations**

### 2. In-port (`api` module)
- Add method to existing interface or create new interface in `kz.dev.api.auth`
- Interface only — no implementation here

### 3. Use-case (`use-case` module)
- Add method to `AuthDomainService` or `OtpDomainService` (whichever fits), or create a new service
- Implement the in-port interface method
- Inject only `spi` interfaces — no Spring annotations except `@Transactional` if needed

### 4. REST layer (`api-adapter-rest`)
- Add request/response DTOs in `kz.dev.api.adapter.rest.dto` — use `@Data` Lombok
- Add method to `AuthClient` (`@HttpExchange`) with correct HTTP annotation
- Implement in `AuthController` — inject the in-port interface, never the use-case class directly
- Map domain exceptions to `ProblemDetail` in `GlobalExceptionHandler` if new exception added

### 5. Security config (`microservice`)
- If endpoint is public: add path to `SecurityConfig` permit list
- If new bean needed: add `@Bean` to `AppConfig` with constructor injection

### 6. DB migration — only if schema changes
- Create `microservice/src/main/resources/db/migration/V{N}__<snake_description>.sql`
- Use next version number (check existing files first)

## Rules reminder
- File path before every code block
- No `@Autowired` — constructor injection only
- No placeholders — complete working code
- `core` module: zero framework annotations
